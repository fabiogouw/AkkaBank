package com.galore.bank;

import java.io.Serializable;

public class NewAccount implements Serializable {

    private final String _id;

    public NewAccount() {
        _id = "1";
    }

    public NewAccount(String id) {
        _id = id;
    }

    public String getId() {
        return _id;
    }
}