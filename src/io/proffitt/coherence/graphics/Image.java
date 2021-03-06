package io.proffitt.coherence.graphics;

import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;
import io.proffitt.coherence.math.Matrix4f;
import io.proffitt.coherence.math.Vector4f;
import io.proffitt.coherence.resource.Texture;

public class Image {
	Texture					tex;
	static final float[]	imgVerts	= { 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 1, 1, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 1, 1, 0, 0, 1, 0, 0, 1, 0 };
	static final Model		model		= new Model(imgVerts);
	public Image(Texture t) {
		tex = t;
	}
	public Texture getBackingTexture() {
		return tex;
	}
	/**
	 * fewer param version of {@link Text#draw(Vector4f, float, float)
	 * draw(pos, hOffset, vOffset)}
	 */
	public void draw(Vector4f pos) {
		draw(pos, 0);
	}
	/**
	 * fewer param version of {@link Text#draw(int, int, float, float)
	 * draw(x, y, hOffset, vOffset)}
	 */
	public void draw(int x, int y) {
		draw(x, y, 0);
	}
	/**
	 * fewer param version of {@link Text#draw(Vector4f, float, float)
	 * draw(pos, hOffset, vOffset)}
	 */
	public void draw(Vector4f pos, float offset) {
		draw(pos, offset, offset);
	}
	/**
	 * fewer param version of {@link Text#draw(int, int, float, float)
	 * draw(x, y, hOffset, vOffset)}
	 */
	public void draw(int x, int y, float offset) {
		draw(x, y, offset, offset);
	}
	/**
	 * @param pos
	 *            position in window
	 * @param hOffset
	 *            horizontal alignment (0 for left align at pos, 1 for right
	 *            align, 0.5f for centered)
	 * @param vOffset
	 *            vertical alignment about pos
	 */
	public void draw(Vector4f pos, float hOffset, float vOffset) {
		glUniformMatrix4fv(3, false, Matrix4f.getTranslation(pos.x - (tex.width * hOffset), pos.y - (tex.height * vOffset), pos.z).multiply(Matrix4f.getScale(tex.width, tex.height, 1)).toFloatBuffer());// model
		tex.bind();
		model.render();
	}
	/**
	 * @param x
	 *            horizontal position in window
	 * @param y
	 *            vertical position in window
	 * @param hOffset
	 *            horizontal alignment (0 for left align at pos, 1 for right
	 *            align, 0.5f for centered)
	 * @param vOffset
	 *            vertical alignment about pos
	 */
	public void draw(int x, int y, float hOffset, float vOffset) {
		glUniformMatrix4fv(3, false, Matrix4f.getTranslation(x - (tex.width * hOffset), y - (tex.height * vOffset), 0).multiply(Matrix4f.getScale(tex.width, tex.height, 1)).toFloatBuffer());// model
		tex.bind();
		model.render();
	}
	public void destroy() {
		if (tex != null) {
			tex.destroy();
			tex = null;
		}
	}
}
