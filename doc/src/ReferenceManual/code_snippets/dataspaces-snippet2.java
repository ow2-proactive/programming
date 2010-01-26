public void addInputClicked() {
    PADataSpaces.addInput(gui.getName(), gui.getURL()), gui.getPath());
    informOthers();
}

public void processAllInputs() {
    for (Entry<String, DataSpacesFileObject> input : PADataSpaces.resolveAllKnownInputs()) {
        String name = input.getKey();
        DataSpacesFileObject inputDir = input.getValue();

        if (isAlreadyProcessed(name))
            continue;
        DataSpacesFileObject children[] = inputDir.getChildren();
        // process each file in each input...
    }
}
