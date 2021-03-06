package io.proffitt.coherence.resource;

import io.proffitt.coherence.graphics.Model;
import io.proffitt.coherence.gui.Menu;
import io.proffitt.coherence.gui.MenuComponent;
import io.proffitt.coherence.settings.Configuration;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Scanner;

import javax.imageio.ImageIO;

public class ResourceHandler {
	private static ResourceHandler	rh;
	public static ResourceHandler get() {
		if (rh == null) {
			rh = new ResourceHandler();
		}
		return rh;
	}
	private HashMap<String, Configuration>	configs;
	private HashMap<String, MenuComponent>	menus;
	private HashMap<String, Texture>		textures;
	private HashMap<String, Font>			fonts;
	private HashMap<String, Shader>			shaders;
	private HashMap<String, Model>			models;
	private ResourceHandler() {
		configs = new HashMap<String, Configuration>();
		menus = new HashMap<String, MenuComponent>();
		textures = new HashMap<String, Texture>();
		fonts = new HashMap<String, Font>();
		shaders = new HashMap<String, Shader>();
		models = new HashMap<String, Model>();
	}
	/**
	 * Removes and cleans up all cached resources. ResourceHandler may still be
	 * used after this, but accessing any resource will require it to be
	 * reloaded from disk (or generated) and into memory (and/or onto the GPU).
	 */
	public void cleanup() {
		//this.saveConfig("settings");//save settings
		this.saveConfig("keybindings");//save bound keys
		for (String s : configs.keySet().toArray(new String[0])) {
			configs.remove(s);// configs don't need cleanup
		}
		for (String s : menus.keySet().toArray(new String[0])) {
			menus.remove(s);// menus don't need cleanup
		}
		for (String s : textures.keySet().toArray(new String[0])) {
			textures.remove(s).destroy();
		}
		for (String s : fonts.keySet().toArray(new String[0])) {
			fonts.remove(s);// fonts don't need cleanup
		}
		for (String s : shaders.keySet().toArray(new String[0])) {
			shaders.remove(s).destroy();
		}
		for (String s : models.keySet().toArray(new String[0])) {
			models.remove(s).destroy();
		}
	}
	public Configuration getConfig(String name) {
		if (!configs.containsKey(name)) {
			configs.put(name, loadConfig(name));
		}
		return configs.get(name);
	}
	private Configuration loadConfig(String name) {
		Configuration ret = new Configuration();
		ret.loadFromCML(this.loadResourceAsCML("res/config/" + name + ".cml"));
		return ret;
	}
	public MenuComponent getMenu(String name) {
		if (!menus.containsKey(name)) {
			menus.put(name, loadMenu(name));
		}
		return menus.get(name);
	}
	private MenuComponent loadMenu(String name) {
		return Menu.createFromCML(loadResourceAsCML("res/gui/" + name + ".mnu").getTag("root"));
	}
	public Texture getTexture(String name) {
		if (!textures.containsKey(name)) {
			textures.put(name, loadTexture(name));
		}
		return textures.get(name);
	}
	private Texture loadTexture(String name) {
		BufferedImage img = null;
		try {
			img = ImageIO.read(new File("res/tex/" + name + ".png"));
		} catch (IOException e) {
			System.out.println("Could not load texture \"" + name + "\"");
			e.printStackTrace();
		}
		return new Texture(img);
	}
	public Font getFont(String name) {
		if (!fonts.containsKey(name)) {
			fonts.put(name, loadFont(name));
		}
		return fonts.get(name);
	}
	private Font loadFont(String name) {
		String fontData[] = name.split(",");
		return new Font(fontData[0].trim(), Integer.parseInt(fontData[1].trim()));
	}
	public Shader getShader(String name) {
		if (!shaders.containsKey(name)) {
			shaders.put(name, loadShader(name));
		}
		return shaders.get(name);
	}
	private Shader loadShader(String name) {
		return new Shader(loadResourceAsString("res/shader/" + name + "_vertex.glsl"), loadResourceAsString("res/shader/" + name + "_fragment.glsl"));
	}
	public Model getModel(String name) {
		if (!models.containsKey(name)) {
			models.put(name, loadModel(name));
		}
		return models.get(name);
	}
	private Model loadModel(String name) {
		String file = this.loadResourceAsString("res/model/" + name + ".obj");
		String[] lines = file.split("\n");
		int facenum = 0;
		int vertnum = 0;
		int normnum = 0;
		for (int i = 0; i < lines.length; i++) {
			if (lines[i].startsWith("f")) {
				facenum++;
			} else if (lines[i].startsWith("vn")) {
				normnum++;
			} else if (lines[i].startsWith("v")) {
				vertnum++;
			}
		}
		float[] rawverts = new float[vertnum * 3];
		int vertpos = 0;
		float[] rawnorms = new float[normnum * 3];
		int normpos = 0;
		for (int i = 0; i < lines.length; i++) {
			if (lines[i].startsWith("vn")) {
				String[] components = lines[i].split(" ");
				rawnorms[normpos] = Float.parseFloat(components[1]);
				rawnorms[normpos + 1] = Float.parseFloat(components[2]);
				rawnorms[normpos + 2] = Float.parseFloat(components[3]);
				normpos += 3;
			} else if (lines[i].startsWith("v")) {
				String[] components = lines[i].split(" ");
				rawverts[vertpos] = Float.parseFloat(components[1]);
				rawverts[vertpos + 1] = Float.parseFloat(components[2]);
				rawverts[vertpos + 2] = Float.parseFloat(components[3]);
				vertpos += 3;
			}
		}
		float[] verts = new float[facenum * 18];
		int facepos = 0;
		for (int i = 0; i < lines.length; i++) {
			if (lines[i].startsWith("f")) {
				String[] components = lines[i].split(" ");
				for (int k = 0; k < 3; k++) {
					String[] c = components[k + 1].split("//");
					int vpos = (Integer.parseInt(c[0]) - 1) * 3;
					int npos = (Integer.parseInt(c[1]) - 1) * 3;
					for (int j = 0; j < 3; j++) {
						verts[facepos + j] = rawverts[vpos + j];
					}
					for (int j = 0; j < 3; j++) {
						verts[facepos + j + 3] = rawnorms[npos + j];
					}
					facepos += 6;
				}
			}
		}
		Model m = new Model(verts);
		return m;
	}
	private CMLTag loadResourceAsCML(String path) {
		CMLTag ret = new CMLTag("", loadResourceAsString(path).replace("\t", "    "));
		return ret;
	}
	private void saveConfig(String name) {
		this.saveResourceAsString("res/config/" + name + ".cml", configs.get(name).toString());
	}
	private String loadResourceAsString(String path) {
		Scanner scanner = null;
		try {
			scanner = new Scanner(new File(path));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		StringBuilder contents = new StringBuilder("");
		while (scanner.hasNextLine()) {
			contents.append(scanner.nextLine()).append("\n");
		}
		scanner.close();
		return contents.toString();
	}
	private void saveResourceAsString(String path, String contents) {
		System.out.println("Saving: " + path);
		PrintWriter pw;
		try {
			pw = new PrintWriter(path);
			pw.println(contents);
			pw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
