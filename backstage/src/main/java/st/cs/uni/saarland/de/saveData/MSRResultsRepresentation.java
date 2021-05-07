package st.cs.uni.saarland.de.saveData;

/**
 * Created by avdiienko on 15/12/15.
 */
public class MSRResultsRepresentation {
    public final MSRApiRepresentation benignApps;
    public final MSRApiRepresentation malciousApps;

    public MSRResultsRepresentation(){
        this.benignApps = new MSRApiRepresentation();
        this.malciousApps = new MSRApiRepresentation();
    }

    @Override
    public boolean equals(Object o){
        if(!(o instanceof MSRResultsRepresentation)){
            return false;
        }
        MSRResultsRepresentation toCompare = (MSRResultsRepresentation)o;
        return benignApps.equals(toCompare.benignApps) && malciousApps.equals(toCompare.malciousApps);
    }
}
