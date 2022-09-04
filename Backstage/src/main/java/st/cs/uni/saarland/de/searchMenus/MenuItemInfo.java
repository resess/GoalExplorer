package st.cs.uni.saarland.de.searchMenus;

import org.apache.commons.lang3.StringUtils;
import st.cs.uni.saarland.de.helpClasses.Info;
import st.cs.uni.saarland.de.reachabilityAnalysis.AnalyseIntentSwitch;
import st.cs.uni.saarland.de.reachabilityAnalysis.IntentInfo;

import java.util.Objects;

public class MenuItemInfo extends Info {
    private String parentMenu = "";
    private String idReg = "";
    private Integer id;
    private String text = "";
    private IntentInfo intentInfo;
    private AnalyseIntentSwitch intentSwitch;
    private String intentInfoReg = "";

    public MenuItemInfo(String reg) {
        super(reg);
    }

    public MenuItemInfo(String parentMenu, String idReg, Integer id, String text){
        this("",parentMenu,idReg, id, text, "", null);
    }

    public MenuItemInfo(String reg, String parentMenu, String idReg, Integer id, String text, String intentInfoReg, IntentInfo intentInfo){
        this(reg);
        this.parentMenu = parentMenu;
        this.idReg = idReg;
        this.id = id;
        this.text = text;
        this.intentInfoReg = intentInfoReg;
        this.intentInfo = intentInfo;
    }




    public Integer getId() {
        return id;
    }

    public String getIdReg() {
        return idReg;
    }

    public String getParentMenu() {
        return parentMenu;
    }

    @Override
    public String getText() {
        return text;
    }

    public IntentInfo getIntentInfo() {
        return intentInfo;
    }

    public String getIntentInfoReg() {
        return intentInfoReg;
    }

    public AnalyseIntentSwitch getIntentSwitch() {
        return intentSwitch;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setIdReg(String idReg) {
        this.idReg = idReg;
    }

    public void setParentMenu(String parentMenu) {
        this.parentMenu = parentMenu;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setIntentInfo(IntentInfo intentInfo) {
        this.intentInfo = intentInfo;
    }

    public void setIntentInfoReg(String intentInfoReg) {
        this.intentInfoReg = intentInfoReg;
    }

    public void setIntentSwitch(AnalyseIntentSwitch intentSwitch) {
        this.intentSwitch = intentSwitch;
    }

    @Override
    public Info clone() {
        return new MenuItemInfo(searchedEReg, parentMenu, idReg, id, text, intentInfoReg, intentInfo);
    }

    @Override
    public boolean allValuesFound() {
        //We resolve the intent info
        return id != null && intentInfo != null && StringUtils.isBlank(intentInfoReg) && StringUtils.isBlank(idReg);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        MenuItemInfo that = (MenuItemInfo) o;
        return Objects.equals(idReg, that.idReg) && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), idReg, id);
    }
}
