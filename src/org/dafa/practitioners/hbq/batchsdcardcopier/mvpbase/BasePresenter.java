package org.dafa.practitioners.hbq.batchsdcardcopier.mvpbase;

public class BasePresenter<T extends MvpView> implements MvpPresenter<T> {
	private T view;

	public BasePresenter(T view) {
		this.view = view;
	}

	@Override
	public T getView() {
		return view;
	}
}
