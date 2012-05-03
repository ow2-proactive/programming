package functionalTests.component.nonfunctional.adl.factory;

public class WrapperAttributesImpl implements WrapperAttributes {

	private String header = "";
	private int count = 0;
	
	@Override
	public String getHeader() {
		return header;
	}

	@Override
	public void setHeader(final String header) {
		this.header = header;
	}

	@Override
	public int getCount() {
		return count;
	}

	@Override
	public void setCount(final int count) {
		this.count = count;
	}

}
