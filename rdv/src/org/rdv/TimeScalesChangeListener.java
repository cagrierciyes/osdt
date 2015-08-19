
package org.rdv;

public interface TimeScalesChangeListener {
    void timeScaleAdded(int index, double timeScale);
    void timeScaleRemoved(int index, double timeScale);
}
