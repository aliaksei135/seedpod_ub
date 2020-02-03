package seedpod.agents.meta;

import repast.simphony.data2.AggregateDataSource;
import seedpod.agents.BaseAircraftAgent;

public class AirproxAggregateDataSource implements AggregateDataSource {
	
	private String id;
	private Class<?> sourceType;
	private Class<?> countType;
	
	public AirproxAggregateDataSource(String id, Class<?> sourceType) {
		this.id = id;
		this.sourceType = sourceType;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public Class<Integer> getDataType() {
		return Integer.class;
	}

	@Override
	public Class<?> getSourceType() {
		return sourceType;
	}

	@Override
	public Object get(Iterable<?> objs, int size) {
		int count = 0;
		for (Object o : objs) {
			if(o.getClass().equals(this.countType)) {
				count++;
			}
		}
		return count;
	}

	@Override
	public void reset() {
	}

}
