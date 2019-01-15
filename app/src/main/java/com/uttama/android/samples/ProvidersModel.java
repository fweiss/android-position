package com.uttama.android.samples;

import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

/**
 * This MVC model provides state, status, and information of the available providers.
 * It is generally speaking a facade for list of LocationProviders, but it also provides 
 * for enabling selected providers and emitting MVC notifications for state changes.
 * @author frankw
 *
 */
public class ProvidersModel {
	// maybe map name -> LocationProvider?
	private Set<String> providers = new HashSet<String>();
	private Set<String> enabledProviders;
	private Observable enableProviderObservable = new Observable();
	public ProvidersModel() {
		enabledProviders = new HashSet<String>();
	}
	public void setProviderEnabled(String provider, boolean enable) {
		// TODO: test for provider available
		boolean wasEnabled = enabledProviders.contains(provider);
		boolean changed = wasEnabled && ! enable || ! wasEnabled && enable;
		if (enable) {
			enabledProviders.add(provider);
		} else {
			enabledProviders.remove(provider);
		}
		if (changed) {
			enableProviderObservable.notifyObservers(provider);
		}
	}
	public boolean isProviderEnabled(String provider) {
		return enabledProviders.contains(provider);
	}
	public void addEnableProviderObserver(Observer observer) {
		enableProviderObservable.addObserver(observer);
	}
	public void removeEnableProviderObserver(Observer observer) {
		enableProviderObservable.deleteObserver(observer);
	}
}
