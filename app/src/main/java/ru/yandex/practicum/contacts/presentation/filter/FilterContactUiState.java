package ru.yandex.practicum.contacts.presentation.filter;

import java.util.Collections;
import java.util.Set;

import ru.yandex.practicum.contacts.model.ContactType;

public class FilterContactUiState {
    public boolean isApplyEnable = false;
    public Set<ContactType> newSelectedContactTypes = Collections.emptySet();
}
