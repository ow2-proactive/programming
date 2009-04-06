package functionalTests.component.webservices;

public interface HelloWorldItf {

    public void putHelloWorld();

    public void putTextToSay(String textToSay);

    public String sayText();

    public Boolean contains(String textToCheck);
}
