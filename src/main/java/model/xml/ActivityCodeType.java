package model.xml;

import org.jdom2.Element;

public class ActivityCodeType {
    private String name;
    private int objectId;

    public ActivityCodeType(Element activityCodeTypeElement) {
        this.name = activityCodeTypeElement.getChild(XMLTypes.NAME).getText();
        this.objectId = Integer.parseInt(activityCodeTypeElement.getChild(XMLTypes.OBJECT_ID).getText());
    }

    public String getName() {
        return name;
    }

    public int getObjectId() {
        return objectId;
    }
}
