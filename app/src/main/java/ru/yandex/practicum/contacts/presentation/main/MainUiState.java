package ru.yandex.practicum.contacts.presentation.main;

import java.util.Collections;
import java.util.Set;

import ru.yandex.practicum.contacts.model.ContactType;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

class MainUiState {

    private boolean searchVisibility = false;
    private boolean resetSearchButtonVisibility = false;

    public Actions actions = new Actions();
    public MenuBadges menuBadges = new MenuBadges();

    public void setSearchVisibility(final boolean searchVisibility)
    {
        this.searchVisibility = searchVisibility;
    }
    public boolean searchVisibility()
    {
        return searchVisibility;
    }
    public boolean resetSearchButtonVisibility()
    {
        return resetSearchButtonVisibility;
    }

    public void setResetSearchButtonVisibility(final boolean resetSearchButtonVisibility)
    {
        this.resetSearchButtonVisibility = resetSearchButtonVisibility;
    }

    @NonNull
    public MainUiState copy() {
        final MainUiState copy = new MainUiState();
        copy.searchVisibility = searchVisibility;
        copy.resetSearchButtonVisibility = resetSearchButtonVisibility;
        copy.actions = actions.copy();
        copy.menuBadges = menuBadges.copy();
        return copy;
    }

    public static class Actions {

        public Action<Boolean> finishActivity = new Action<>(false);
        public Action<String> showSortTypeDialog = new Action<>(null);
        public Action<Set<ContactType>> showFilterContactTypeDialog = new Action<>(Collections.emptySet());

        @NonNull
        public Actions copy() {
            final Actions copy = new Actions();
            copy.finishActivity = new Action<>(finishActivity.data);
            copy.showSortTypeDialog = new Action<>(showSortTypeDialog.data);
            copy.showFilterContactTypeDialog = new Action<>(showFilterContactTypeDialog.data);
            return copy;
        }

        public void clear() {
            finishActivity.data = false;
            showSortTypeDialog.data = null;
            showFilterContactTypeDialog.data = Collections.emptySet();
        }
    }

    public static class Action<T> {

        @Nullable
        public T data;

        public Action(@Nullable T value) {
            this.data = value;
        }
    }

    public static class MenuBadges {

        @Nullable
        public MenuBadge sort = null;
        @Nullable
        public MenuBadge filter = null;
        @Nullable
        public MenuBadge search = null;

        public MenuBadges copy() {
            final MenuBadges copy = new MenuBadges();
            copy.sort = sort == null ? null : new MenuBadge(sort.value);
            copy.filter = filter == null ? null : new MenuBadge(filter.value);
            copy.search = search == null ? null : new MenuBadge(search.value);
            return copy;
        }
    }

    public static class MenuBadge {

        public int value;

        public MenuBadge(int value) {
            this.value = value;
        }
    }
}
