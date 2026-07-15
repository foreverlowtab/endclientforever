package forever.end.client.ecf.ui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.joml.Matrix4f;

/** Низкоуровневая отрисовка простой цветной геометрии в мире (POSITION_COLOR quads). */
public final class Render3D {
    private Render3D() {}

    private static RenderType layer() {
        return RenderType.debugQuads();
    }

    private static int a(int c) { return (c >>> 24) & 0xFF; }
    private static int r(int c) { return (c >> 16) & 0xFF; }
    private static int g(int c) { return (c >> 8) & 0xFF; }
    private static int b(int c) { return c & 0xFF; }

    /** Один четырёхугольник по 4 точкам. */
    public static void quad(PoseStack ps, MultiBufferSource buf,
                            float x0, float y0, float z0, float x1, float y1, float z1,
                            float x2, float y2, float z2, float x3, float y3, float z3, int color) {
        Matrix4f m = ps.last().pose();
        VertexConsumer vc = buf.getBuffer(layer());
        int A = a(color), R = r(color), G = g(color), B = b(color);
        vc.addVertex(m, x0, y0, z0).setColor(R, G, B, A);
        vc.addVertex(m, x1, y1, z1).setColor(R, G, B, A);
        vc.addVertex(m, x2, y2, z2).setColor(R, G, B, A);
        vc.addVertex(m, x3, y3, z3).setColor(R, G, B, A);
    }

    /** Треугольник (вырожденный quad: 4-я точка совпадает с 3-й). */
    public static void tri(PoseStack ps, MultiBufferSource buf,
                           float x0, float y0, float z0, float x1, float y1, float z1,
                           float x2, float y2, float z2, int color) {
        quad(ps, buf, x0, y0, z0, x1, y1, z1, x2, y2, z2, x2, y2, z2, color);
    }

    /** Горизонтальное кольцо на высоте y вокруг локального центра. */
    public static void ringXZ(PoseStack ps, MultiBufferSource buf, float y, float rInner, float rOuter, int seg, int color) {
        for (int i = 0; i < seg; i++) {
            double a0 = (Math.PI * 2 * i) / seg, a1 = (Math.PI * 2 * (i + 1)) / seg;
            float c0 = (float) Math.cos(a0), s0 = (float) Math.sin(a0);
            float c1 = (float) Math.cos(a1), s1 = (float) Math.sin(a1);
            quad(ps, buf,
                    c0 * rInner, y, s0 * rInner,
                    c0 * rOuter, y, s0 * rOuter,
                    c1 * rOuter, y, s1 * rOuter,
                    c1 * rInner, y, s1 * rInner, color);
        }
    }

    /** Заполненный горизонтальный диск на высоте y. */
    public static void discXZ(PoseStack ps, MultiBufferSource buf, float y, float radius, int seg, int color) {
        for (int i = 0; i < seg; i++) {
            double a0 = (Math.PI * 2 * i) / seg, a1 = (Math.PI * 2 * (i + 1)) / seg;
            float c0 = (float) Math.cos(a0), s0 = (float) Math.sin(a0);
            float c1 = (float) Math.cos(a1), s1 = (float) Math.sin(a1);
            quad(ps, buf, 0, y, 0, c0 * radius, y, s0 * radius, c1 * radius, y, s1 * radius, 0, y, 0, color);
        }
    }

    /** Конус с основанием радиуса r на высоте y0 и вершиной на y0+height. */
    public static void cone(PoseStack ps, MultiBufferSource buf, float y0, float r, float height, int seg, int color) {
        float ay = y0 + height;
        int under = shade(color, 0.7f);
        for (int i = 0; i < seg; i++) {
            double a0 = (Math.PI * 2 * i) / seg, a1 = (Math.PI * 2 * (i + 1)) / seg;
            float c0 = (float) Math.cos(a0), s0 = (float) Math.sin(a0);
            float c1 = (float) Math.cos(a1), s1 = (float) Math.sin(a1);
            quad(ps, buf, c0 * r, y0, s0 * r, c1 * r, y0, s1 * r, 0, ay, 0, 0, ay, 0, color);
            quad(ps, buf, c1 * r, y0, s1 * r, c0 * r, y0, s0 * r, 0, y0, 0, 0, y0, 0, under);
        }
    }

    /** Параллелепипед по двум углам (локальные координаты). */
    public static void box(PoseStack ps, MultiBufferSource buf,
                           float x0, float y0, float z0, float x1, float y1, float z1, int color) {
        int side = shade(color, 0.85f), topc = shade(color, 1.08f), botc = shade(color, 0.6f);
        quad(ps, buf, x0, y1, z0, x0, y1, z1, x1, y1, z1, x1, y1, z0, topc);
        quad(ps, buf, x0, y0, z1, x0, y0, z0, x1, y0, z0, x1, y0, z1, botc);
        quad(ps, buf, x1, y0, z0, x0, y0, z0, x0, y1, z0, x1, y1, z0, side);
        quad(ps, buf, x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1, side);
        quad(ps, buf, x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0, side);
        quad(ps, buf, x1, y0, z1, x1, y0, z0, x1, y1, z0, x1, y1, z1, side);
    }

    /** Умножить RGB на коэффициент (для граней), альфа сохраняется. */
    public static int shade(int c, float f) {
        int A = (c >>> 24) & 0xFF;
        int R = Math.min(255, (int) (((c >> 16) & 0xFF) * f));
        int G = Math.min(255, (int) (((c >> 8) & 0xFF) * f));
        int B = Math.min(255, (int) ((c & 0xFF) * f));
        return (A << 24) | (R << 16) | (G << 8) | B;
    }
}
