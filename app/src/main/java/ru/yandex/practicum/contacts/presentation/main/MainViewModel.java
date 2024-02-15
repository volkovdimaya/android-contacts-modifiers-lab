package ru.yandex.practicum.contacts.presentation.main;

import android.app.Application;
import android.text.TextUtils;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import ru.yandex.practicum.contacts.interactor.ContactMerger;
import ru.yandex.practicum.contacts.mapper.ContactUiMapper;
import ru.yandex.practicum.contacts.model.Contact;
import ru.yandex.practicum.contacts.model.ContactSource;
import ru.yandex.practicum.contacts.model.ContactType;
import ru.yandex.practicum.contacts.model.MergedContact;
import ru.yandex.practicum.contacts.presentation.main.model.MenuClick;
import ru.yandex.practicum.contacts.presentation.sort.model.SortType;
import ru.yandex.practicum.contacts.repository.ContactRepository;
import ru.yandex.practicum.contacts.repository.ContactSourceRepository;
import ru.yandex.practicum.contacts.utils.java.ThreadUtils;
import ru.yandex.practicum.contacts.utils.model.MergedContactUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class MainViewModel extends AndroidViewModel {

    private final ContactSourceRepository contactSourceRepository;
    private final ContactRepository contactRepository;
    private final ContactMerger contactMerger;
    private final ContactUiMapper uiMapper;

    private final MutableLiveData<List<ContactUi>> contactsLiveDate = new MutableLiveData<>();
    private final MutableLiveData<MainUiState> uiStateLiveDate = new MutableLiveData<>();

    private final MainState state = new MainState();
    private final MainUiState uiState = new MainUiState();

    public MainViewModel(@NonNull Application application) {
        super(application);
        contactSourceRepository = new ContactSourceRepository(application);
        contactRepository = new ContactRepository(application);
        contactMerger = new ContactMerger();
        uiMapper = new ContactUiMapper();
        ThreadUtils.runAsync(this::initLoading);
    }

    public LiveData<List<ContactUi>> getContactsLiveDate() {
        return contactsLiveDate;
    }

    public MutableLiveData<MainUiState> getUiStateLiveDate() {
        return uiStateLiveDate;
    }

    public void initLoading() {
        final Set<ContactSource> sources = contactSourceRepository.getAllContactSources();
        final List<String> sourceNames = sources.stream()
                .map(ContactSource::getName)
                .collect(Collectors.toList());
        final List<Contact> contacts = contactRepository.getContacts(sourceNames);

        final List<MergedContact> allContacts = contactMerger.getMergedContacts(contacts, sources);
        state.setAllContacts(allContacts);
        mapContactsAndUpdate();
    }

    public void search() {
        mapContactsAndUpdate();
    }

    public void onMenuClick(MenuClick click) {
        switch (click) {
            case SORT:
                uiState.actions.showSortTypeDialog.data = state.getSortType();
                break;
            case FILTER:
                uiState.actions.showFilterContactTypeDialog.data = new HashSet<>(state.getContactTypes());
                break;
            case SEARCH:
                uiState.setSearchVisibility(!uiState.searchVisibility());
                break;
        }
        updateUiState();
    }

    public void updateSortType(String sortType) {
        state.setSortType(sortType);
        updateBadges();
        mapContactsAndUpdate();
    }

    public void updateFilterContactTypes(Set<ContactType> filterContactTypes) {
        state.setContactTypes(filterContactTypes);
        updateBadges();
        mapContactsAndUpdate();
    }

    public void onBackPressed() {
        if (uiState.searchVisibility()) {
            uiState.setSearchVisibility(false);
        } else {
            uiState.actions.finishActivity.data = true;
        }
        updateUiState();
    }

    public void updateSearchText(String query) {
        state.setQuery(query);
        uiState.setResetSearchButtonVisibility(state.getQuery().length() != 0);
        updateUiState();
    }

    private void updateBadges() {
        if (!state.getSortType().equals(state.getDefaultSortType())) {
            uiState.menuBadges.sort = new MainUiState.MenuBadge(0);
        } else {
            uiState.menuBadges.sort = null;
        }

        if (!state.getContactTypes().equals(state.getDefaultContactTypes())) {
            uiState.menuBadges.filter = new MainUiState.MenuBadge(state.getContactTypes().size());
        } else {
            uiState.menuBadges.filter = null;
        }

        updateUiState();
    }

    private void mapContactsAndUpdate() {
        final List<ContactUi> uiContacts = state.getAllContacts().stream()
                .filter(contact -> MergedContactUtils.contains(contact, state.getQuery()))
                .filter(contact -> MergedContactUtils.contains(contact, state.getContactTypes()))
                .sorted(createComparator(state.getSortType()))
                .map(uiMapper::map)
                .collect(Collectors.toList());
        contactsLiveDate.postValue(uiContacts);
    }

    private Comparator<MergedContact> createComparator(String type) {
        switch (type) {
            case SortType.BY_NAME:
                return createComparator(MergedContact::getFirstName)
                        .thenComparing(createComparator(MergedContact::getSurname))
                        .thenComparing(createComparator(MergedContact::getNormalizedNumber))
                        .thenComparing(createComparator(MergedContact::getEmail));
            case SortType.BY_NAME_REVERSED:
                return createReversedComparator(MergedContact::getFirstName)
                        .thenComparing(createReversedComparator(MergedContact::getSurname))
                        .thenComparing(createReversedComparator(MergedContact::getNormalizedNumber))
                        .thenComparing(createReversedComparator(MergedContact::getEmail));
            case SortType.BY_SURNAME:
                return createComparator(MergedContact::getSurname)
                        .thenComparing(createComparator(MergedContact::getFirstName))
                        .thenComparing(createComparator(MergedContact::getNormalizedNumber))
                        .thenComparing(createComparator(MergedContact::getEmail));
            case SortType.BY_SURNAME_REVERSED:
                return createReversedComparator(MergedContact::getSurname)
                        .thenComparing(createReversedComparator(MergedContact::getFirstName))
                        .thenComparing(createReversedComparator(MergedContact::getNormalizedNumber))
                        .thenComparing(createReversedComparator(MergedContact::getEmail));
            default:
                throw new IllegalArgumentException("Not supported SortType");
        }
    }

    private Comparator<MergedContact> createComparator(Function<MergedContact, String> keyExtractor) {
        return (left, right) -> {
            final String leftField = keyExtractor.apply(left);
            final String rightField = keyExtractor.apply(right);
            if (!TextUtils.isEmpty(leftField) && !TextUtils.isEmpty(rightField)) {
                return leftField.compareTo(rightField);
            }
            // Empty lines should be after
            if (TextUtils.isEmpty(leftField) && !TextUtils.isEmpty(rightField)) {
                return 1;
            }
            if (!TextUtils.isEmpty(leftField) && TextUtils.isEmpty(rightField)) {
                return -1;
            }
            return 0;
        };
    }

    private Comparator<MergedContact> createReversedComparator(Function<MergedContact, String> keyExtractor) {
        return (left, right) -> {
            final String leftField = keyExtractor.apply(left);
            final String rightField = keyExtractor.apply(right);
            if (!TextUtils.isEmpty(leftField) && !TextUtils.isEmpty(rightField)) {
                return rightField.compareTo(leftField);
            }
            // Empty lines should be after
            if (TextUtils.isEmpty(leftField) && !TextUtils.isEmpty(rightField)) {
                return 1;
            }
            if (!TextUtils.isEmpty(leftField) && TextUtils.isEmpty(rightField)) {
                return -1;
            }
            return 0;
        };
    }

    private void updateUiState() {
        uiStateLiveDate.setValue(uiState.copy());
        uiState.actions.clear();
    }
}
