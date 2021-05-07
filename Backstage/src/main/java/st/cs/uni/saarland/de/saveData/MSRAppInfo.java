package st.cs.uni.saarland.de.saveData;

/**
 * Created by avdiienko on 16/12/15.
 */
public class MSRAppInfo {
    public boolean malicious;
    public String apkName;

    @Override
    public boolean equals(Object obj){
        if(!(obj instanceof  MSRAppInfo))
            return false;
        MSRAppInfo toCompare = (MSRAppInfo)obj;
        return (this.apkName.equals(toCompare.apkName)) && (this.malicious == toCompare.malicious);
    }
}
