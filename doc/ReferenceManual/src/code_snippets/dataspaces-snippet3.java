public void computeAndStore() {
    // compute...
    final DataSpacesFileObject scratchFile = PADataSpaces.resolveScratchForAO("data.txt");
    // write intermediate results there...
    scratchFile.close();
    anotherAO.readMyFile(scratchFile.getURI());
}
public void readMyFile(String uri) {
    DataSpacesFileObject file = PADataSpaces.resolveFile(uri);
    // process this file...
}
