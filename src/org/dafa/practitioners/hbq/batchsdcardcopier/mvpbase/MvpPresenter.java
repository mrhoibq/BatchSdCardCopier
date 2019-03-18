package org.dafa.practitioners.hbq.batchsdcardcopier.mvpbase;

public interface MvpPresenter<T extends MvpView> {
	T getView();
}
