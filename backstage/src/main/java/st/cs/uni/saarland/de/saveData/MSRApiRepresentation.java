package st.cs.uni.saarland.de.saveData;

/**
 * Created by avdiienko on 15/12/15.
 */
public class MSRApiRepresentation {
    public int uiCallbacks;
    public int nonUiCallbacks;

    @Override
    public boolean equals(Object o){
        if(!(o instanceof  MSRApiRepresentation)){
            return false;
        }
        MSRApiRepresentation toCompare = (MSRApiRepresentation)o;
        return uiCallbacks == toCompare.uiCallbacks && nonUiCallbacks == toCompare.nonUiCallbacks;
    }
}
