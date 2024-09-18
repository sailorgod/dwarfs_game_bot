package com.sailordev.dvorfsgamebot.telegram.dto;

public enum UserState {
    SLEEP,
    AWAIT_START,
    AWAIT_MESSAGE_TO_ADMIN,
    AWAIT_ADMINS_RESPONSE,
    AWAIT_USER_SELECTION,
    AWAIT_SET_EVENT_DATE,
    AWAIT_SET_NAME_OR_DESCRIPTION_EVENT,
    AWAIT_REGISTRATION,
    AWAIT_RESPONSE_TO_USER,
    AWAIT_SELECT_ACTION,
    AWAIT_SEND_MESSAGE_TO_USER,
    AWAIT_SET_KEYBOARD,
    AWAIT_EVENT_SET_NAME,
    AWAIT_EVENT_SET_DESCRIPTION,
    AWAIT_SET_NOTIFICATION_TIME,
    AWAIT_SELECT_EDIT_EVENT,
    AWAIT_EDIT_EVENT_NAME,
    AWAIT_EDIT_EVENT_DESCRIPTION,
    AWAIT_EDIT_EVENT_DATE,
    AWAIT_FIRST_SELECT_EDIT_EVENT,
    AWAIT_SELECT_DELETE_EVENT,
    AWAIT_ADD_COORDINATES,
    AWAIT_SELECT_ACTION_COORDINATE,
    AWAIT_ADD_COORDINATE_DESCRIPTION,
    AWAIT_SELECT_DWARF_FOR_ADDED,
    AWAIT_SELECT_EDIT_COORDINATE,
    AWAIT_SELECT_ACTION_EDIT_COORDINATE,
    AWAIT_SELECT_DELETE_COORDINATE,
    AWAIT_SET_DWARF_NAME,
    AWAIT_SELECT_DWARF_ACTION,
    AWAIT_DWARF_DESCRIPTION,
    AWAIT_EDIT_DWARF_NAME,
    AWAIT_SELECT_EDIT_DWARF,
    AWAIT_SELECT_DELETE_DWARF,
    AWAIT_ADD_HINT, AWAIT_EDIT_HINT,
    AWAIT_SELECT_EDIT_HINT,
    AWAIT_DELETE_HINT,
    AWAIT_SELECT_USER_BAN,
    AWAIT_SELECT_UNBLOCK_USER,
    AWAIT_SELECT_ACTION_HINT,
    AWAIT_SELECT_COORDINATE,
    AWAIT_SELECT_COORDINATE_FOR_ADD_HINT,
    AWAIT_ADD_COORDINATE_HINT,
    AWAIT_USER_SELECT_HINT,
    AWAIT_USER_SELECT_COORDINATE,
    BAN
}
