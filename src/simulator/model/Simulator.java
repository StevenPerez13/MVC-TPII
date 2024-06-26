package simulator.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONObject;

import simulator.factories.Factory;
import simulator.model.animal.Animal;
import simulator.model.animal.AnimalInfo;
import simulator.model.animal.State;
import simulator.model.gestorregion.MapInfo;
import simulator.model.gestorregion.RegionManager;
import simulator.model.region.Region;

public class Simulator implements JSONable,Observable<EcoSysObserver> {

	private Factory<Animal> animals_factory;
	private Factory<Region> regions_factory;
	private RegionManager _region_mngr;
	private List<Animal> animalsSimulator;
	private double cur_time;
	private List<EcoSysObserver> observers;

	public Simulator(int cols, int rows, int width, int height, Factory<Animal> animals_factory,
			Factory<Region> regions_factory) {

		if (animals_factory == null) {
			throw new IllegalArgumentException("The animals_factory can´t be null");
		}
		if (regions_factory == null) {
			throw new IllegalArgumentException("The regions_factory can´t be null");
		}
		if (cols <= 0 || rows <= 0 || width <= 0 || height <= 0)
			throw new IllegalArgumentException("The cols, rows, width or height must be a number greater than zero");
		this._region_mngr = new RegionManager(cols, rows, width, height);
		this.animals_factory = animals_factory;
		this.regions_factory = regions_factory;
		this.animalsSimulator = new ArrayList<>();
		this.cur_time = 0.0;
		observers = new ArrayList<>();

	}

	private void set_region(int row, int col, Region r) {
		_region_mngr.set_region(row, col, r);
	}

	public void set_region(int row, int col, JSONObject r_json) {
		if (row < 0 || col < 0 || row >= _region_mngr.get_rows() || col >= _region_mngr.get_cols()) {
			throw new IllegalArgumentException(
					"The range of \"row\" and \"col\" must be within the bounds of the board.");
		}
		Region r = regions_factory.create_instance(r_json);
		set_region(row, col, r);
	}

	private void add_animal(Animal a) {
		animalsSimulator.add(a);
		_region_mngr.register_animal(a);
	}

	public void add_animal(JSONObject a_json) {
		Animal a = animals_factory.create_instance(a_json);
		add_animal(a);
	}

	public List<? extends AnimalInfo> get_animals() {
		List<Animal> animalsSimulatorUnmodifiable = Collections.unmodifiableList(animalsSimulator);
		return animalsSimulatorUnmodifiable;
	}

	public MapInfo get_map_info() {
		return _region_mngr;
	}

	public double get_time() {
		return cur_time;
	}

	private void removeDeads(int begin) {
		// Remove Deads
		for (int i = begin; i >= 0; i--) {
			Animal a = animalsSimulator.get(i);
			if (a.get_state().equals(State.DEAD)) {
				animalsSimulator.remove(a); // Eliminar el animal de la lista animalsSimulator directamente
				_region_mngr.unregister_animal(a);
			}

		}
	}

	public void advance(double dt) {
		setCur_time(cur_time + dt);
		int begin = animalsSimulator.size() - 1;

		for (int i = begin; i >= 0; i--) {
			Animal a = animalsSimulator.get(i);
			a.update(dt);
			_region_mngr.update_animal_region(a);
			if (a.is_pregnant())
				this.add_animal(a.deliver_baby());
		}

		this.removeDeads(begin);
		_region_mngr.update_all_regions(dt);

	}
	@Override
	public void addObserver(EcoSysObserver o) {
		if(!observers.contains(o)) {
			observers.add(o);
			//o.onRegister(cur_time, _region_mngr, animalsSimulator);
		}
		
	}

	@Override
	public void removeObserver(EcoSysObserver o) {
		observers.remove(o);
		
	}

	public JSONObject as_JSON() {
		JSONObject jsonSimulator = new JSONObject();
		jsonSimulator.put("time", cur_time);
		jsonSimulator.put("state", _region_mngr.as_JSON());
		return jsonSimulator;
	}
	
	public void reset(int cols, int rows, int width, int height) {
		this.animalsSimulator.clear();
		this._region_mngr = new RegionManager(cols, rows, width, height);
		this.cur_time = 0.0;
		
		for (EcoSysObserver observer : observers) {
            //observer.onReset(cur_time, _region_mngr, null);
        }
	}

	public Factory<Animal> getAnimals_factory() {
		return animals_factory;
	}

	public void setAnimals_factory(Factory<Animal> animals_factory) {
		this.animals_factory = animals_factory;
	}

	public Factory<Region> getRegions_factory() {
		return regions_factory;
	}

	public void setRegions_factory(Factory<Region> regions_factory) {
		this.regions_factory = regions_factory;
	}

	public RegionManager get_region_mngr() {
		return _region_mngr;
	}

	public void set_region_mngr(RegionManager _region_mngr) {
		this._region_mngr = _region_mngr;
	}

	public List<Animal> getAnimalsSimulator() {
		return animalsSimulator;
	}

	public void setAnimalsSimulator(List<Animal> animalsSimulator) {
		this.animalsSimulator = animalsSimulator;
	}

	public double getCur_time() {
		return cur_time;
	}

	public void setCur_time(double cur_time) {
		this.cur_time = cur_time;
	}

	

}
