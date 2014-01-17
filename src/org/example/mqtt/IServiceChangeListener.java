package org.example.mqtt;


// interface to receive a notification when a service is added or removed
// it was created to let fragments communicate to the Main Acitivity in
// a more Activity independent format

public interface IServiceChangeListener {
	void notifyServiceListChanged();
}
