package seedpod.constants;

public enum EAirspaceClass {

	// Controlled
	CLASS_A("Class A"), CLASS_C("Class C"), CLASS_D("Class D"),
	// Uncontrolled
	CLASS_E("Class E"), CLASS_F("Class F"), // Not used in UK
	CLASS_G("Class G"),
	// Special use
	// Not technically airspace classes but fall into the paradigm here
	DANGER("Danger Area"), PROHIBITED("Prohibited"), RESTRICTED("Restricted");

	private final String name;

	private EAirspaceClass(String name) {
		this.name = name;
	}

	public boolean equalsName(String testName) {
		return name.equals(testName);
	}

	@Override
	public String toString() {
		return this.name;
	}

}