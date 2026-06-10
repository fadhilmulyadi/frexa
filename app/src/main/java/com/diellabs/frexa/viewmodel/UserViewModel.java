package com.diellabs.frexa.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.*;
import com.diellabs.frexa.data.repository.UserRepository;

public class UserViewModel extends AndroidViewModel {
    public final MutableLiveData<Boolean> isLoggedIn = new MutableLiveData<>();
    public final MutableLiveData<String> userName = new MutableLiveData<>();
    private final UserRepository repo;

    public UserViewModel(@NonNull Application app) {
        super(app);
        repo = new UserRepository(app);
        isLoggedIn.setValue(repo.isLoggedIn());
        userName.setValue(repo.getUserName());
    }

    public void login(String name, String cred, boolean isEmail) {
        repo.login(name, cred, isEmail);
        isLoggedIn.setValue(true);
        userName.setValue(name);
    }

    public String getUserName() { return repo.getUserName(); }
    public String getUserEmail() { return repo.getUserEmail(); }
    public String getUserPhone() { return repo.getUserPhone(); }
    public float getBalance() { return repo.getBalance(); }
    public String getTheme() { return repo.getTheme(); }
    public void setTheme(String m) { repo.setTheme(m); }
    public void logout() { repo.logout(); isLoggedIn.setValue(false); }
}
