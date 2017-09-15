package model.xml;

import model.sql.SQLController;
import org.jdom2.Element;

import java.util.ArrayList;
import java.util.HashMap;

public class StepHelper extends HashMap<Integer, ArrayList<Step>> {
    public StepHelper(Element element, SQLController sqlController) {
        for (Element stepElement : element.getChildren()) {
            Step step = new Step(stepElement, sqlController);
            if (this.containsKey(step.getActivityObjectId())) {
                this.get(step.getActivityObjectId()).add(step);
            } else {
                ArrayList<Step> tempStep = new ArrayList<>();
                tempStep.add(step);
                this.put(step.getActivityObjectId(), tempStep);
            }
        }
    }
}
