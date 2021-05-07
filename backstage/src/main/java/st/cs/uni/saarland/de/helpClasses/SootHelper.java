package st.cs.uni.saarland.de.helpClasses;

import soot.*;
import soot.jimple.Jimple;

import java.util.List;

/**
 * Created by avdiienko on 05/05/16.
 */
public class SootHelper {

    public static SootClass createSootClass(String clsName)
    {
        SootClass sc = new SootClass(clsName);
        sc.setSuperclass(Scene.v().getSootClass("java.lang.Object"));

        sc.setApplicationClass();
//        sc.setPhantom(false);
        sc.setInScene(true);

        return sc;
    }

    public static SootMethod createSootMethod(SootClass sootClass, String name, List<Type> paramTypes, Type returnType, boolean isStatic)
    {
        int modifier = Modifier.PUBLIC;

        if (isStatic)
        {
            modifier = modifier | Modifier.STATIC;
        }

        SootMethod sootMethod = new SootMethod(name, paramTypes, returnType, modifier);
        sootClass.addMethod(sootMethod);

        Body body = Jimple.v().newBody(sootMethod);
        sootMethod.setActiveBody(body);

        return sootMethod;
    }
}
