package org.wordpress.pioupiou;

public interface BasePresenter<T> {
    /**
     * Binds presenter with a view when resumed. The Presenter will perform initialization here.
     */
    void takeView(T view);

    void dropView();
}
