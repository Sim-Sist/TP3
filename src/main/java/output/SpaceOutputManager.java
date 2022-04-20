package output;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import particles.Event;
import particles.Particle;
import particles.Space;

public class SpaceOutputManager extends OutputManager {
    private Space s;
    private final String INIT_STATE_DEFAULT_FILENAME = "static-info000.txt";
    private final String DYNAMIC_STATE_DEFAULT_FILENAME = "dynamic-info000.txt";

    public SpaceOutputManager(Space s) {
        this.s = s;
    }

    public boolean outputInitialState() {
        return this.outputInitialState(INIT_STATE_DEFAULT_FILENAME);
    }

    public boolean outputInitialState(String filename) {
        /**
         * static-info
         * - Header:
         * -- tamaño
         * -- cant de partículas
         * - Body:
         * -- radio, color
         */
        StringBuilder content = new StringBuilder();
        // Header
        content.append(s.getSize()).append('\n');
        content.append(s.getParticles().length).append('\n');
        // Separator
        content.append('\n');
        // Body
        for (Particle p : s.getParticles()) {
            content.append(p.radius).append(' ').append(p.color).append('\n');
        }
        return outputStaticFile(filename, content.toString());
    }

    public boolean outputState(int step) {
        return outputState(step, DYNAMIC_STATE_DEFAULT_FILENAME);
    }

    public boolean outputState(int step, String filename) {
        StringBuilder content = new StringBuilder();
        Event e = s.getNextEvent();
        Particle p1 = e.getP1(), p2 = e.getP2();
        /**
         * dynamic-info
         * Body:
         * -- nro evento
         * -- tc
         * -- pc1,pc2 #partículas que colisionaron (siempre son dos, salvo por el estado
         * inicial)
         * -- x y vx vy #una linea por cada partícula
         */
        if (step == 0)
            content.append('\n');
        content.append(step).append('\n');
        content.append(e.getTime()).append('\n');
        content.append(p1.getIndex());
        if (e.isParticleCollision()) {
            content.append(' ').append(p2.getIndex());
        }
        content.append('\n');
        // content.append(p1.getX()).append(' ')
        // .append(p1.getY()).append(' ')
        // .append(p1.getVx()).append(' ')
        // .append(p1.getVy()).append('\n');
        // if (e.isParticleCollision()) {
        // content.append(p2.getX()).append(' ')
        // .append(p2.getY()).append(' ')
        // .append(p2.getVx()).append(' ')
        // .append(p2
        // .getVy()).append('\n');
        // }
        for (Particle p : s.getParticles()) {
            content.append(String.format("%f %f %f %f\n", p.x, p.y, p.getVx(), p.getVy()));
        }
        return outputDynamicFile(step, filename, content.toString());
    }

}