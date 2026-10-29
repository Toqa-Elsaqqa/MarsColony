package Core;

public class MartianSurface implements Location{
    public static final MartianSurface INSTANCE = new MartianSurface();
    private MartianSurface(){}
    
    @Override
    public String getLocation() {
        return "MARTIAN SURFACE";
    }
}