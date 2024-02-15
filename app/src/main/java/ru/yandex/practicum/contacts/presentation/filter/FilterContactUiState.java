package ru.yandex.practicum.contacts.presentation.filter;

import java.util.Collections;
import java.util.Set;

import ru.yandex.practicum.contacts.model.ContactType;

class FilterContactUiState {
    private boolean isApplyEnable = false;
    private Set<ContactType> newSelectedContactTypes = Collections.emptySet();

    public boolean isApplyEnable() {
        return isApplyEnable;
    }

    public void setApplyEnable(final boolean applyEnable) {
        isApplyEnable = applyEnable;
    }

    public Set<ContactType> getNewSelectedContactTypes () {
        return newSelectedContactTypes;
    }

    public void setNewSelectedContactTypes(final Set<ContactType> newSelectedContactTypes) {
        this.newSelectedContactTypes = newSelectedContactTypes;
    }
}
