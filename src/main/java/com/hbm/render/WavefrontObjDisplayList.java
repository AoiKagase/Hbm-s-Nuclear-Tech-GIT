package com.hbm.render;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import com.hbm.hfr.render.loader.HFRWavefrontObject;
import com.hbm.hfr.render.loader.S_GroupObject;
import com.hbm.render.amlfrom1710.GroupObject;
import com.hbm.render.amlfrom1710.IModelCustom;
import com.hbm.render.amlfrom1710.Tessellator;
import com.hbm.render.amlfrom1710.WavefrontObject;

public class WavefrontObjDisplayList implements IModelCustom {

	public List<Pair<String, Integer>> nameToCallList = new ArrayList<>();
	private final Map<String, List<Integer>> callListsByName = new HashMap<>();
	
	public WavefrontObjDisplayList(WavefrontObject obj) {
		Tessellator tes = Tessellator.instance;
		for(GroupObject g : obj.groupObjects){
			int list = GL11.glGenLists(1);
			GL11.glNewList(list, GL11.GL_COMPILE);
			tes.startDrawing(g.glDrawingMode);
			g.render(tes);
			tes.draw();
			GL11.glEndList();
			nameToCallList.add(Pair.of(g.name, list));
			callListsByName.computeIfAbsent(g.name.toLowerCase(Locale.ROOT), key -> new ArrayList<>()).add(list);
		}
		
	}
	
	public WavefrontObjDisplayList(HFRWavefrontObject obj) {
		for(S_GroupObject g : obj.groupObjects){
			int list = GL11.glGenLists(1);
			GL11.glNewList(list, GL11.GL_COMPILE);
			g.render();
			GL11.glEndList();
			nameToCallList.add(Pair.of(g.name, list));
			callListsByName.computeIfAbsent(g.name.toLowerCase(Locale.ROOT), key -> new ArrayList<>()).add(list);
		}
	}

	public int getListForName(String name){
		List<Integer> matchingLists = callListsByName.get(name.toLowerCase(Locale.ROOT));
		if(matchingLists != null && !matchingLists.isEmpty()) {
			return matchingLists.get(0);
		}
		return 0;
	}

	@Override
	public String getType() {
		return "obj_list";
	}

	@Override
	public void renderAll() {
		for(Pair<String, Integer> p : nameToCallList)
			GL11.glCallList(p.getRight());
	}

	@Override
	public void renderOnly(String... groupNames) {
		Set<String> requested = new HashSet<>();
		for(String name : groupNames) {
			requested.add(name.toLowerCase(Locale.ROOT));
		}
		for(Pair<String, Integer> p : nameToCallList) {
			if(requested.contains(p.getLeft().toLowerCase(Locale.ROOT))) {
				GL11.glCallList(p.getRight());
			}
		}
	}

	@Override
	public void renderPart(String partName) {
		List<Integer> matchingLists = callListsByName.get(partName.toLowerCase(Locale.ROOT));
		if(matchingLists != null) {
			for(Integer list : matchingLists) {
				GL11.glCallList(list);
			}
		}
	}

	@Override
	public void renderAllExcept(String... excludedGroupNames) {
		Set<String> excluded = new HashSet<>();
		for(String name : excludedGroupNames) {
			excluded.add(name.toLowerCase(Locale.ROOT));
		}
		for(Pair<String, Integer> p : nameToCallList){
			if(!excluded.contains(p.getLeft().toLowerCase(Locale.ROOT))){
				GL11.glCallList(p.getRight());
			}
		}
	}

	@Override
	public void tessellateAll(Tessellator tes){
		throw new RuntimeException("Tessellate operation not supported on display list object");
	}

	@Override
	public void tessellatePart(Tessellator tes, String name){
		throw new RuntimeException("Tessellate operation not supported on display list object");
	}

	@Override
	public void tessellateOnly(Tessellator tes, String... names){
		throw new RuntimeException("Tessellate operation not supported on display list object");
	}

	@Override
	public void tessellateAllExcept(Tessellator tes, String... excluded){
		throw new RuntimeException("Tessellate operation not supported on display list object");
	}
}
