package org.yourorghere;

import com.sun.opengl.util.Animator;
import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureIO;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLException;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;



/**
 * Mono.java <BR>
 * author: Brian Paul (converted to Java by Ron Cemer and Sven Goethel) <P>
 *
 * This version is equal to Brian Paul's version 1.2 1999/10/21
 */
public class Mono implements GLEventListener,MouseListener, MouseMotionListener, KeyListener {

    Texture t,c,p;
    static File im;
    Clip clip;
    private float view_rotx = 0.01f,view_roty = 0.01f;
    private int OldMX,OldMY;
    boolean[] keys = new boolean[256];
    
    //Posicion del mono en la ventana
    
    private static final float POS_X = 0f;
    private static final float POS_Y = -0.08f;
    private static final float POS_Z = 0f;
    private static final float ANCHO_MANOS = 0.1f;
    private static final float ANCHO_DEDOS = 0.424f;
    private static final float LARGO_BRAZOS = 0.33f;
    private static final float ANCHO_BRAZOS = 0.04f;
    private static final int SLICES = 40;
    private static final int STACKS = 40;
    float rquad = 0.15f;
    private GLUquadric q = null;
    private static int mvt = 0;
    private static final float ANCHO_PUPILAS = 0.03f;
    private static final float ANCHO_BOCA_ABIERTA = 0.1f;
    static GLCanvas canvas;
    
    static String salto = "src/sonidos/gunshot.wav"; //sonidos
    static String corre = "src/sonidos/sirena.wav";

    
    public static void main(String[] args) {
        //im = new File("") ruta de la imagen de fondo
        Frame frame = new Frame("Mono");
        GLCanvas canvas = new GLCanvas();

        canvas.addGLEventListener(new Mono());
        frame.add(canvas);
        frame.setSize(800, 800);
        final Animator animator = new Animator(canvas);
        frame.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                // Run this on another thread than the AWT event queue to
                // make sure the call to Animator.stop() completes before
                // exiting
                new Thread(new Runnable() {

                    public void run() {
                        animator.stop();
                        System.exit(0);
                    }
                }).start();
            }
        });
        // Center frame
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        animator.start();
    }

    public void init(GLAutoDrawable drawable) {
        
        GL gl = drawable.getGL();
        System.err.println("INIT GL IS: " + gl.getClass().getName());
        gl.setSwapInterval(1);
        
        //Establecer la luz
        
        
        float light_ambient[] = {0.9f, 0.9f, 0.9f, 1.0f};
        float light_diffuse[] = {0.3f, 0.3f, 0.3f, 1.0f};
        float light_specular[] = {1.0f, 1.0f, 1.0f, 1.0f};
        float light_position[] = {1.0f, 1.5f, 1.0f, 0.0f};

        gl.glLightfv(GL.GL_LIGHT0, GL.GL_AMBIENT, light_ambient, 0);
        gl.glLightfv(GL.GL_LIGHT0, GL.GL_DIFFUSE, light_diffuse, 0);
        gl.glLightfv(GL.GL_LIGHT0, GL.GL_SPECULAR, light_specular, 0);
        gl.glLightfv(GL.GL_LIGHT0, GL.GL_POSITION, light_position, 0);

        gl.glEnable(GL.GL_LIGHTING);
        gl.glEnable(GL.GL_LIGHT0);
        gl.glEnable(GL.GL_DEPTH_TEST);

        // Setup the drawing area and shading mode
        gl.glClearColor(0.7f, 0.7f, 0.7f, 0.7f);
        gl.glShadeModel(GL.GL_SMOOTH); // try setting this to GL_FLAT and see what happens.
        
        drawable.addMouseListener(this);
        drawable.addMouseMotionListener(this);
        drawable.addKeyListener(this);
        
        //Imagenes de fondo
          
        /*try {
            File im2 = new File("src/imagenes/techo.jpg");
            c = TextureIO.newTexture(im2, true);

            File im3 = new File("src/imagenes/suelo.jpg");
            p = TextureIO.newTexture(im3, true);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
*/
    }
    
    //Evento presionar tecla

    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL gl = drawable.getGL();
        GLU glu = new GLU();

        if (height <= 0) { // avoid a divide by zero error!
        
            height = 1;
        }
        final float h = (float) width / (float) height;
        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluPerspective(45.0f, h, 1.0, 20.0);
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glLoadIdentity();
    }

    public void display(GLAutoDrawable drawable) {
        GL gl = drawable.getGL();
        GLU glu = new GLU();
        
        // Clear the drawing area
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        gl.glMatrixMode(GL.GL_MODELVIEW);

        // Reset the current matrix to the "identity"
        gl.glLoadIdentity();
        
        //Vista de los Ojos
        
        glu.gluLookAt(0.1f, 0.0f, 4.0f,// eye
                0.0f, 0.0f, 0.0f, // looking at
                0.0f, 0.0f, 1.0f // is up
        );

        
        //Mover todo el escenario
        
        gl.glTranslatef(POS_X, POS_Y, POS_Z);
        gl.glRotatef(view_rotx, 1.0f, 0.0f, 0.0f);
        gl.glRotatef(view_roty, 0.0f, 1.0f, 0.0f);
        gl.glRotatef(90, 0.0f, 0.0f, 1.0f);
        
       /* cielo(gl, glu, c);
        pasto(gl, glu, p);
        try {
            t = TextureIO.newTexture(im, true);
        } catch (IOException ex) {
            Logger.getLogger(Mono.class.getName()).log(Level.SEVERE, null, ex);
        } catch (GLException ex) {
            Logger.getLogger(Mono.class.getName()).log(Level.SEVERE, null, ex);
        }
        
         fondo(gl, glu, t);

*/
         
         Mono mono = new Mono();
         mono.drawCuerpo(gl, glu);
        //dibujar mono
        
        
        // Flush all drawing operations to the graphics card
        gl.glFlush();
    }

    
    public void cielo(GL gl, GLU glu, Texture t) {
        int m = t.getTextureObject();
        gl.glEnable(GL.GL_TEXTURE_2D);
        gl.glBindTexture(GL.GL_TEXTURE_2D, m);
        gl.glBegin(gl.GL_QUADS);
        gl.glTexCoord2d(0.0f, 1.0f);
        gl.glVertex3f(6.0f, 6.0f, 6f);
        gl.glTexCoord2d(1.0f, 1.0f);
        gl.glVertex3f(6.0f, 6f, -6f);
        gl.glTexCoord2d(1.0f, 0.0f);
        gl.glVertex3f(-6.0f, 6.0f, -6f);
        gl.glTexCoord2d(0.0f, 0.0f);
        gl.glVertex3f(-6.0f, 6.0f, 6f);
        gl.glEnd();
        gl.glFlush();
        gl.glDisable(gl.GL_TEXTURE_2D);
    }

    public void pasto(GL gl, GLU glu, Texture t) {
        int m = t.getTextureObject();
        gl.glEnable(GL.GL_TEXTURE_2D);
        gl.glBindTexture(GL.GL_TEXTURE_2D, m);
        gl.glBegin(gl.GL_QUADS);
        gl.glTexCoord2d(0.0f, 1.0f);
        gl.glVertex3f(6.0f, -6.0f, 6f);
        gl.glTexCoord2d(1.0f, 1.0f);
        gl.glVertex3f(6.0f, -6f, -6f);
        gl.glTexCoord2d(1.0f, 0.0f);
        gl.glVertex3f(-6.0f, -6.0f, -6f);
        gl.glTexCoord2d(0.0f, 0.0f);
        gl.glVertex3f(-6.0f, -6.0f, 6f);
        gl.glEnd();
        gl.glFlush();
        gl.glDisable(gl.GL_TEXTURE_2D);
    }
    
    
    
     public void fondo(GL gl, GLU glu, Texture t) {
        int m = t.getTextureObject();
        gl.glEnable(GL.GL_TEXTURE_2D);
        gl.glBindTexture(GL.GL_TEXTURE_2D, m);
        gl.glBegin(gl.GL_QUADS);
        gl.glTexCoord2d(0.0f, 1.0f);
        gl.glVertex3f(-6.0f, -6.0f, -6f);
        gl.glTexCoord2d(1.0f, 1.0f);
        gl.glVertex3f(6.0f, -6f, -6f);
        gl.glTexCoord2d(1.0f, 0.0f);
        gl.glVertex3f(6.0f, 6.0f, -6f);
        gl.glTexCoord2d(0.0f, 0.0f);
        gl.glVertex3f(-6.0f, 6.0f, -6f);

        gl.glTexCoord2d(0.0f, 1.0f);
        gl.glVertex3f(6.0f, -6.0f, -6f);
        gl.glTexCoord2d(1.0f, 1.0f);
        gl.glVertex3f(6.0f, -6f, 6f);
        gl.glTexCoord2d(1.0f, 0.0f);
        gl.glVertex3f(6.0f, 6.0f, 6f);
        gl.glTexCoord2d(0.0f, 0.0f);
        gl.glVertex3f(6.0f, 6.0f, -6f);

        gl.glTexCoord2d(0.0f, 1.0f);
        gl.glVertex3f(-6.0f, -6.0f, -6f);
        gl.glTexCoord2d(1.0f, 1.0f);
        gl.glVertex3f(-6.0f, -6f, 6f);
        gl.glTexCoord2d(1.0f, 0.0f);
        gl.glVertex3f(-6.0f, 6.0f, 6f);
        gl.glTexCoord2d(0.0f, 0.0f);
        gl.glVertex3f(-6.0f, 6.0f, -6f);

        gl.glTexCoord2d(0.0f, 1.0f);
        gl.glVertex3f(-6.0f, -6.0f, 6f);
        gl.glTexCoord2d(1.0f, 1.0f);
        gl.glVertex3f(6.0f, -6f, 6f);
        gl.glTexCoord2d(1.0f, 0.0f);
        gl.glVertex3f(6.0f, 6.0f, 6f);
        gl.glTexCoord2d(0.0f, 0.0f);
        gl.glVertex3f(-6.0f, 6.0f, 6f);

        gl.glEnd();
        gl.glFlush();
        gl.glDisable(gl.GL_TEXTURE_2D);
    }

    
    public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {
    }

    public void mouseClicked(MouseEvent me) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void mousePressed(MouseEvent me) {
       OldMX = me.getX();
       OldMY = me.getY();
    }

    public void mouseReleased(MouseEvent me) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void mouseEntered(MouseEvent me) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void mouseExited(MouseEvent me) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void mouseDragged(MouseEvent me) {
          int x = me.getX();
        int y = me.getY();
        Dimension size = me.getComponent().getSize();
        float thetaX = 360.0f * ((float) (x - OldMX) / (float) size.width);
        float thetaY = 360.0f * ((float) (OldMY - y) / (float) size.height);
        OldMX = x;
        OldMY = y;
        view_rotx += thetaX;
        view_roty += thetaY;
    }

    public void mouseMoved(MouseEvent me) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void keyTyped(KeyEvent ke) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void keyPressed(KeyEvent ke) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void keyReleased(KeyEvent ke) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
        public void reproducir(String efecto) {
        try {
            clip = AudioSystem.getClip();
            clip.open(AudioSystem.getAudioInputStream(new File(efecto)));
            clip.start();
            //clip.loop(1000);

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
        
          public static void fondo(GL gl, Texture t) {
        int m = t.getTextureObject();
        gl.glEnable(GL.GL_TEXTURE_2D);
        gl.glBindTexture(GL.GL_TEXTURE_2D, m);
        gl.glBegin(gl.GL_QUADS);
        gl.glTexCoord2d(0.0f, 1.0f);
        gl.glVertex3f(-6.0f, -6.0f, -6f);
        gl.glTexCoord2d(1.0f, 1.0f);
        gl.glVertex3f(6.0f, -6f, -6f);
        gl.glTexCoord2d(1.0f, 0.0f);
        gl.glVertex3f(6.0f, 6.0f, -6f);
        gl.glTexCoord2d(0.0f, 0.0f);
        gl.glVertex3f(-6.0f, 6.0f, -6f);

        gl.glTexCoord2d(0.0f, 1.0f);
        gl.glVertex3f(6.0f, -6.0f, -6f);
        gl.glTexCoord2d(1.0f, 1.0f);
        gl.glVertex3f(6.0f, -6f, 6f);
        gl.glTexCoord2d(1.0f, 0.0f);
        gl.glVertex3f(6.0f, 6.0f, 6f);
        gl.glTexCoord2d(0.0f, 0.0f);
        gl.glVertex3f(6.0f, 6.0f, -6f);

        gl.glTexCoord2d(0.0f, 1.0f);
        gl.glVertex3f(-6.0f, -6.0f, -6f);
        gl.glTexCoord2d(1.0f, 1.0f);
        gl.glVertex3f(-6.0f, -6f, 6f);
        gl.glTexCoord2d(1.0f, 0.0f);
        gl.glVertex3f(-6.0f, 6.0f, 6f);
        gl.glTexCoord2d(0.0f, 0.0f);
        gl.glVertex3f(-6.0f, 6.0f, -6f);

        gl.glTexCoord2d(0.0f, 1.0f);
        gl.glVertex3f(-6.0f, -6.0f, 6f);
        gl.glTexCoord2d(1.0f, 1.0f);
        gl.glVertex3f(6.0f, -6f, 6f);
        gl.glTexCoord2d(1.0f, 0.0f);
        gl.glVertex3f(6.0f, 6.0f, 6f);
        gl.glTexCoord2d(0.0f, 0.0f);
        gl.glVertex3f(-6.0f, 6.0f, 6f);

        gl.glEnd();
        gl.glFlush();
        gl.glDisable(gl.GL_TEXTURE_2D);
    }
          public void drawEsponja(GL gl, boolean walk, boolean jump, boolean traslacion, boolean escalacion, boolean rotacion, boolean corte, boolean reflex){
               GLU glu = new GLU();
        q = glu.gluNewQuadric();
        glu.gluQuadricDrawStyle(q, GLU.GLU_FILL);
        glu.gluQuadricOrientation(q, GLU.GLU_OUTSIDE);
        glu.gluQuadricNormals(q, GLU.GLU_SMOOTH);
        
        drawCuerpo(gl,glu);
        
          }
          
              public void drawCuerpo(GL gl, GLU glu){
                  
                  set_yellow_material(gl);
                  gl.glPushMatrix();
                  gl.glTranslatef(0.0f, 0.5f, 0f);
                  gl.glPopMatrix();
                  
                  //Frente
                  
                   gl.glBegin(GL.GL_QUADS);
                   gl.glVertex3f(-0.25f, 0, .3f);
                   gl.glVertex3f(.55f, 0, .3f);
                   gl.glVertex3f(.55f, .9f, .3f);
                   gl.glVertex3f(-.25f, .9f, .3f);
                   
                   
                   gl.glEnd();
                   
                   //Ojos
                   
                    
                   
        
          /* set_grey_material(gl);
        gl.glPushMatrix();
        gl.glTranslatef(-0.07f, 0.35f, 0.3f);
        glu.gluSphere(q, ANCHO_PUPILAS, SLICES, STACKS);
        gl.glTranslatef(0.14f, 0f, 0f);
        glu.gluSphere(q, ANCHO_PUPILAS, SLICES, STACKS);
        gl.glPopMatrix();
                    */
        
                   
                   //Derecha
                   
                    gl.glBegin(GL.GL_QUADS);
                    gl.glVertex3f(0.55f, 0, .3f);
                    gl.glVertex3f(.55f, 0, -.2f);
                    gl.glVertex3f(.55f, .9f, -.2f);
                    gl.glVertex3f(.55f, .9f, .3f);
                    gl.glEnd();
                    
        //Trasera
                    
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex3f(-0.25f, 0, -.2f);
        gl.glVertex3f(.55f, .0f, -.2f);
        gl.glVertex3f(.55f, .9f, -.2f);
        gl.glVertex3f(-.25f, .9f, -.2f);

        gl.glEnd();
                    
        //Izquierda
        
         gl.glBegin(GL.GL_QUADS);
        gl.glVertex3f(-0.25f, 0, -.2f);
        gl.glVertex3f(-.25f, .9f, -.2f);
        gl.glVertex3f(-.25f, .9f, .3f);
        gl.glVertex3f(-.25f, .0f, .3f);
        
        //Superior 
        
         gl.glBegin(GL.GL_QUADS);
        gl.glVertex3f(-0.25f, .5f, .3f);
        gl.glVertex3f(.55f, .5f, .3f);
        gl.glVertex3f(.55f, .5f, -.2f);
        gl.glVertex3f(-.25f, .5f, -.2f);
        
        //Inferior
        
           gl.glBegin(GL.GL_QUADS);
        gl.glVertex3f(-0.25f, .0f, .3f);
        gl.glVertex3f(.55f, .0f, .3f);
        gl.glVertex3f(.55f, .0f, -.2f);
        gl.glVertex3f(-.25f, .0f, -.2f);
        gl.glEnd();
        
        //Pantalones cuadrados
        
        set_white_material(gl);
        
                   //Frente
                  
                   gl.glBegin(GL.GL_QUADS);
                   gl.glVertex3f(-0.25f, 0, .3f);
                   gl.glVertex3f(.55f, 0, .3f);
                   gl.glVertex3f(.55f, -.1f, .3f);
                   gl.glVertex3f(-.25f, -.1f, .3f);
                   
                   
                   gl.glEnd();
                   
                   //Derecha
                   
                    gl.glBegin(GL.GL_QUADS);
                    gl.glVertex3f(0.55f, 0, .3f);
                    gl.glVertex3f(.55f, 0, -.2f);
                    gl.glVertex3f(.55f, -.1f, -.2f);
                    gl.glVertex3f(.55f, -.1f, .3f);
                    gl.glEnd();
                    
        //Trasera
                    
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex3f(-0.25f, 0, -.2f);
        gl.glVertex3f(.55f, .0f, -.2f);
        gl.glVertex3f(.55f, -.1f, -.2f);
        gl.glVertex3f(-.25f, -.1f, -.2f);

        gl.glEnd();
                    
        //Izquierda
        
         gl.glBegin(GL.GL_QUADS);
        gl.glVertex3f(-0.25f, 0, -.2f);
        gl.glVertex3f(-.25f, -.1f, -.2f);
        gl.glVertex3f(-.25f, -.1f, .3f);
        gl.glVertex3f(-.25f, .0f, .3f);
        
        //Superior 
        /*
         gl.glBegin(GL.GL_QUADS);
        gl.glVertex3f(-0.25f, .05f, .3f);
        gl.glVertex3f(.55f, .05f, .3f);
        gl.glVertex3f(.55f, .05f, -.2f);
        gl.glVertex3f(-.25f, .05f, -.2f);
        */
        //Inferior
        
           gl.glBegin(GL.GL_QUADS);
        gl.glVertex3f(-0.25f, -.1f, .3f);
        gl.glVertex3f(.55f, -.1f, .3f);
        gl.glVertex3f(.55f, -.1f, -.2f);
        gl.glVertex3f(-.25f, -.1f, -.2f);
        gl.glEnd();
        
        //Pantalones cuadrados cafes
        
        set_brown_material(gl);
        
                   //Frente
                  
                   gl.glBegin(GL.GL_QUADS);
                   gl.glVertex3f(-0.25f, -.1f, .3f);
                   gl.glVertex3f(.55f, -.1f, .3f);
                   gl.glVertex3f(.55f, -.4f, .3f);
                   gl.glVertex3f(-.25f, -.4f, .3f);
                   
                   
                   gl.glEnd();
                   
                   //Derecha
                   
                    gl.glBegin(GL.GL_QUADS);
                    gl.glVertex3f(0.55f, -.1f, .3f);
                    gl.glVertex3f(.55f, -.1f, -.2f);
                    gl.glVertex3f(.55f, -.4f, -.2f);
                    gl.glVertex3f(.55f, -.4f, .3f);
                    gl.glEnd();
                    
        //Trasera
                    
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex3f(-0.25f, -.1f, -.2f);
        gl.glVertex3f(.55f, -.1f, -.2f);
        gl.glVertex3f(.55f, -.4f, -.2f);
        gl.glVertex3f(-.25f, -.4f, -.2f);

        gl.glEnd();
                    
        //Izquierda
        
         gl.glBegin(GL.GL_QUADS);
        gl.glVertex3f(-0.25f, -.1f, -.2f);
        gl.glVertex3f(-.25f, -.4f, -.2f);
        gl.glVertex3f(-.25f, -.4f, .3f);
        gl.glVertex3f(-.25f, -.1f, .3f);
        
        //Superior 
        /*
         gl.glBegin(GL.GL_QUADS);
        gl.glVertex3f(-0.25f, .05f, .3f);
        gl.glVertex3f(.55f, .05f, .3f);
        gl.glVertex3f(.55f, .05f, -.2f);
        gl.glVertex3f(-.25f, .05f, -.2f);
        */
        //Inferior
        
           gl.glBegin(GL.GL_QUADS);
        gl.glVertex3f(-0.25f, -.4f, .3f);
        gl.glVertex3f(.55f, -.4f, .3f);
        gl.glVertex3f(.55f, -.4f, -.2f);
        gl.glVertex3f(-.25f, -.4f, -.2f);
        gl.glEnd();
        
        
        
        
        //Hombro Izquierdo
        
        set_white_material(gl);
        
         gl.glBegin(GL.GL_QUADS);
        gl.glVertex3f(-0.4f, .5f, .05f);
        gl.glVertex3f(-.4f, .5f, -.1f);
        gl.glVertex3f(-.25f, .5f, -.1f);
        gl.glVertex3f(-.25f, .5f, .05f);
       // gl.glVertex3f(.4f, .0f, .3f);
        gl.glEnd();
        
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex3f(-0.4f, .4f, .05f);
        gl.glVertex3f(-.2f, .4f, .05f);
        gl.glVertex3f(-.2f, .5f, 0.05f);
        gl.glVertex3f(-.4f, .5f, 0.05f);
       // gl.glVertex3f(.4f, .0f, .3f);
        gl.glEnd();
        
          gl.glBegin(GL.GL_QUADS);
        gl.glVertex3f(-0.4f, .4f, .05f);
        gl.glVertex3f(-.2f, .4f, .05f);
        gl.glVertex3f(-.2f, .5f, -0.1f);
        gl.glVertex3f(-.4f, .5f, -0.1f);
       // gl.glVertex3f(.4f, .0f, .3f);
        gl.glEnd();
        
        //Hombro Derecho
        
        set_white_material(gl);
        
         gl.glBegin(GL.GL_QUADS);
        gl.glVertex3f(0.55f, .5f, .05f);
        gl.glVertex3f(0.55f, .5f, -.1f);
        gl.glVertex3f(.65f, .5f, -.1f);
        gl.glVertex3f(.65f, .5f, .05f);
       // gl.glVertex3f(.4f, .0f, .3f);
        gl.glEnd();
        
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex3f(0.40f, .4f, .05f);
        gl.glVertex3f(0.65f, .4f, .05f);
        gl.glVertex3f(.65f, .5f, 0.05f);
        gl.glVertex3f(.40f, .5f, 0.05f);
       // gl.glVertex3f(.4f, .0f, .3f);
        gl.glEnd();
        
          gl.glBegin(GL.GL_QUADS);
        gl.glVertex3f(0.4f, .4f, .05f);
        gl.glVertex3f(0.65f, .4f, .05f);
        gl.glVertex3f(.65f, .5f, -0.1f);
        gl.glVertex3f(.40f, .5f, -0.1f);
       // gl.glVertex3f(.4f, .0f, .3f);
        gl.glEnd();
                  
                  set_eyes_material(gl);

        gl.glPushMatrix();
        gl.glTranslatef(-0.08f, 0.45f, 0.205f);
         glu.gluSphere(q, ANCHO_PUPILAS, SLICES, STACKS);
        gl.glTranslatef(0.16f, 0.0f, 0.0f);
        glu.gluSphere(q, ANCHO_PUPILAS, SLICES, STACKS);
        gl.glPopMatrix();
                  
        

        
        
                    
                  

          }
              
                 public void set_yellow_material(GL gl) {

        float[] mat_ambient = {1f, 1f, 0f, 0.2f};
        float[] mat_diffuse = {1f, 1f, 0f, 1.0f};
        float[] mat_specular = {1f, 1f, 0f, 1.0f};
        float shine = 15.0f;

        gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT, mat_ambient, 0);
        gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_DIFFUSE, mat_diffuse, 0);
        gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_SPECULAR, mat_specular, 0);
        gl.glMaterialf(GL.GL_FRONT_AND_BACK, GL.GL_SHININESS, shine);

    }
           public void set_white_material(GL gl) {

        float[] mat_ambient = {1f, 1f, 1f, 0.2f};
        float[] mat_diffuse = {1f, 1f, 1f, 1.0f};
        float[] mat_specular = {1f, 1f, 1f, 1.0f};
        float shine = 15.0f;

        gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT, mat_ambient, 0);
        gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_DIFFUSE, mat_diffuse, 0);
        gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_SPECULAR, mat_specular, 0);
        gl.glMaterialf(GL.GL_FRONT_AND_BACK, GL.GL_SHININESS, shine);
         
    }
           
           public void set_brown_material(GL gl) {

        float[] mat_ambient = {0.647f, 0.165f, 0.165f, 0.2f};
        float[] mat_diffuse = {0.647f, 0.165f, 0.165f, 1.0f};
        float[] mat_specular = {0.647f, 0.165f, 0.165f, 1.0f};
        float shine = 15.0f;

        gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT, mat_ambient, 0);
        gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_DIFFUSE, mat_diffuse, 0);
        gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_SPECULAR, mat_specular, 0);
        gl.glMaterialf(GL.GL_FRONT_AND_BACK, GL.GL_SHININESS, shine);
         
    }
           
           public void box(GL gl) {
        gl.glBegin(GL.GL_POLYGON);/* f1: front */
        gl.glNormal3f(-1.0f, 0.0f, 0.0f);
        gl.glVertex3f(0.0f, 0.0f, 0.0f);
        gl.glVertex3f(0.0f, 0.0f, 1.0f);
        gl.glVertex3f(1.0f, 0.0f, 1.0f);
        gl.glVertex3f(1.0f, 0.0f, 0.0f);
        gl.glEnd();
        gl.glBegin(GL.GL_POLYGON);/* f2: bottom */
        gl.glNormal3f(0.0f, 0.0f, -1.0f);
        gl.glVertex3f(0.0f, 0.0f, 0.0f);
        gl.glVertex3f(1.0f, 0.0f, 0.0f);
        gl.glVertex3f(1.0f, 1.0f, 0.0f);
        gl.glVertex3f(0.0f, 1.0f, 0.0f);
        gl.glEnd();
        gl.glBegin(GL.GL_POLYGON);/* f3:back */
        gl.glNormal3f(1.0f, 0.0f, 0.0f);
        gl.glVertex3f(1.0f, 1.0f, 0.0f);
        gl.glVertex3f(1.0f, 1.0f, 1.0f);
        gl.glVertex3f(0.0f, 1.0f, 1.0f);
        gl.glVertex3f(0.0f, 1.0f, 0.0f);
        gl.glEnd();
        gl.glBegin(GL.GL_POLYGON);/* f4: top */
        gl.glNormal3f(0.0f, 0.0f, 1.0f);
        gl.glVertex3f(1.0f, 1.0f, 1.0f);
        gl.glVertex3f(1.0f, 0.0f, 1.0f);
        gl.glVertex3f(0.0f, 0.0f, 1.0f);
        gl.glVertex3f(0.0f, 1.0f, 1.0f);
        gl.glEnd();
        gl.glBegin(GL.GL_POLYGON);/* f5: left */
        gl.glNormal3f(0.0f, 1.0f, 0.0f);
        gl.glVertex3f(0.0f, 0.0f, 0.0f);
        gl.glVertex3f(0.0f, 1.0f, 0.0f);
        gl.glVertex3f(0.0f, 1.0f, 1.0f);
        gl.glVertex3f(0.0f, 0.0f, 1.0f);
        gl.glEnd();
        gl.glBegin(GL.GL_POLYGON);/* f6: right */
        gl.glNormal3f(0.0f, -1.0f, 0.0f);
        gl.glVertex3f(1.0f, 0.0f, 0.0f);
        gl.glVertex3f(1.0f, 0.0f, 1.0f);
        gl.glVertex3f(1.0f, 1.0f, 1.0f);
        gl.glVertex3f(1.0f, 1.0f, 0.0f);
        gl.glEnd();

    }
           
          public void set_eyes_material(GL gl) {

        float mat_ambient[] = {1.0f, 1.0f, 1.0f, 1.0f};
        float mat_diffuse[] = {1.0f, 1.0f, 1.0f, 1.0f};
        float mat_specular[] = {0.8f, 0.8f, 0.8f, 1.0f};
        float shine = 51.2f;

        gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT, mat_ambient, 0);
        gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_DIFFUSE, mat_diffuse, 0);
        gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_SPECULAR, mat_specular, 0);
        gl.glMaterialf(GL.GL_FRONT_AND_BACK, GL.GL_SHININESS, shine);

    }

          
           public void set_grey_material(GL gl) {

        float mat_ambient[] = {.75f, .75f, 0.75f, 0.0f};
        float mat_diffuse[] = {1.0f, 1.0f, 1.0f, 1.0f};
        float mat_specular[] = {0.8f, 0.8f, 0.8f, 1.0f};
        float shine = 125.2f;

        gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT, mat_ambient, 0);
        gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_DIFFUSE, mat_diffuse, 0);
        gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_SPECULAR, mat_specular, 0);
        gl.glMaterialf(GL.GL_FRONT_AND_BACK, GL.GL_SHININESS, shine);

    }

}

    

