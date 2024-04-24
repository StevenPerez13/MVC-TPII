package simulator.control;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import simulator.model.Simulator;
import simulator.model.animal.AnimalInfo;
import simulator.model.gestorregion.MapInfo;
import simulator.view.SimpleObjectViewer;
import simulator.view.SimpleObjectViewer.ObjInfo;

public class Controler {
	private Simulator _sim;

	public Controler(Simulator sim) {
		this._sim = sim;
	}

	public void load_data(JSONObject data) {

		if (data.has("regions")) {
			JSONArray regionsArray = data.getJSONArray("regions");
			for (int i = 0; i < regionsArray.length(); i++) {
				JSONObject region = regionsArray.getJSONObject(i);

				JSONArray row = region.getJSONArray("row");
				int rf = row.getInt(0);
				int rt = row.getInt(1);

				JSONArray col = region.getJSONArray("col");
				int cf = col.getInt(0);
				int ct = col.getInt(1);

				JSONObject spec = region.getJSONObject("spec");
				for (int r = rf; r <= rt; r++) {
					for (int c = cf; c <= ct; c++) {
						_sim.set_region(r, c, spec);
					}
				}
			}
		}
		if (!data.has("animals")) 
			throw new IllegalArgumentException("There is no \"animals\" key in the input file.");
		JSONArray animalsArray = data.getJSONArray("animals");
		for (int i = 0; i < animalsArray.length(); i++) {
			JSONObject animal = animalsArray.getJSONObject(i);
			int N = animal.getInt("amount");

			JSONObject spec = animal.getJSONObject("spec");
			for (int r = 0; r < N; r++) {
				_sim.add_animal(spec);
			}
		}

	}

	public void run(double t, double dt, boolean sv, OutputStream out) {

		SimpleObjectViewer view = null;
		if (sv) {
			MapInfo m = _sim.get_map_info();
			view = new SimpleObjectViewer("[ECOSYSTEM]", m.get_width(), m.get_height(), m.get_cols(), m.get_rows());
			view.update(to_animals_info(_sim.get_animals()), _sim.get_time(), dt);
		}

		JSONObject init_state = _sim.as_JSON();
		while (_sim.get_time() <= t) {
			_sim.advance(dt);
			if (sv)
				view.update(to_animals_info(_sim.get_animals()), _sim.get_time(), dt);

		}
		JSONObject final_state = _sim.as_JSON();
		JSONObject outJSON = new JSONObject();
		outJSON.put("in", init_state);
		outJSON.put("out", final_state);

		String jsonStr = outJSON.toString();
		PrintStream p = new PrintStream(out);
		p.println(jsonStr);
		p.close();

		if (sv)
			view.close();
	}

	private List<ObjInfo> to_animals_info(List<? extends AnimalInfo> animals) {
		List<ObjInfo> ol = new ArrayList<>(animals.size());
		for (AnimalInfo a : animals)
			ol.add(new ObjInfo(a.get_genetic_code(), (int) a.get_position().getX(), (int) a.get_position().getY(),
					8));
					//(int) Math.round(a.get_age()) + 2));
		return ol;
	}

}
