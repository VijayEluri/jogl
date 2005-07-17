/*
 * Copyright (c) 2003 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * 
 * - Redistribution of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * 
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 * 
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES,
 * INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN
 * MICROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL NOT BE LIABLE FOR
 * ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR
 * ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR
 * DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE
 * DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY,
 * ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF
 * SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 * 
 * You acknowledge that this software is not designed or intended for use
 * in the design, construction, operation or maintenance of any nuclear
 * facility.
 * 
 * Sun gratefully acknowledges that this software was originally authored
 * and developed by Kenneth Bradley Russell and Christopher John Kline.
 */

package net.java.games.jogl.impl;

import java.util.*;
import net.java.games.jogl.*;

/** Encapsulates the implementation of most of the GLAutoDrawable's
    methods to be able to share it between GLCanvas and GLJPanel. */

public class GLDrawableHelper {
  private volatile List listeners = new ArrayList();
  private static final boolean DEBUG = Debug.debug("GLDrawableHelper");
  private static final boolean VERBOSE = Debug.verbose();
  private boolean autoSwapBufferMode = true;

  public GLDrawableHelper() {
  }

  public synchronized void addGLEventListener(GLEventListener listener) {
    List newListeners = (List) ((ArrayList) listeners).clone();
    newListeners.add(listener);
    listeners = newListeners;
  }
  
  public synchronized void removeGLEventListener(GLEventListener listener) {
    List newListeners = (List) ((ArrayList) listeners).clone();
    newListeners.remove(listener);
    listeners = newListeners;
  }

  public void init(GLAutoDrawable drawable) {
    for (Iterator iter = listeners.iterator(); iter.hasNext(); ) {
      ((GLEventListener) iter.next()).init(drawable);
    }
  }

  public void display(GLAutoDrawable drawable) {
    for (Iterator iter = listeners.iterator(); iter.hasNext(); ) {
      ((GLEventListener) iter.next()).display(drawable);
    }
  }

  public void reshape(GLAutoDrawable drawable,
                      int x, int y, int width, int height) {
    for (Iterator iter = listeners.iterator(); iter.hasNext(); ) {
      ((GLEventListener) iter.next()).reshape(drawable, x, y, width, height);
    }
  }

  public void setAutoSwapBufferMode(boolean onOrOff) {
    autoSwapBufferMode = onOrOff;
  }

  public boolean getAutoSwapBufferMode() {
    return autoSwapBufferMode;
  }

  private static final ThreadLocal perThreadInitAction = new ThreadLocal();
  /** Principal helper method which runs a Runnable with the context
      made current. This could have been made part of GLContext, but a
      desired goal is to be able to implement the GLCanvas in terms of
      the GLContext's public APIs, and putting it into a separate
      class helps ensure that we don't inadvertently use private
      methods of the GLContext or its implementing classes. */
  public void invokeGL(GLDrawable drawable,
                       GLContext context,
                       Runnable  runnable,
                       Runnable  initAction) {
    // Support for recursive makeCurrent() calls as well as calling
    // other drawables' display() methods from within another one's
    GLContext lastContext    = GLContext.getCurrent();
    Runnable  lastInitAction = (Runnable) perThreadInitAction.get();
    if (lastContext != null) {
      lastContext.release();
    }
    
    int res = 0;
    try {
      res = context.makeCurrent();
      if (res != GLContext.CONTEXT_NOT_CURRENT) {
        if (res == GLContext.CONTEXT_CURRENT_NEW) {
          if (DEBUG) {
            System.err.println("GLDrawableHelper " + this + ".invokeGL(): Running initAction");
          }
          initAction.run();
        }
        if (DEBUG && VERBOSE) {
          System.err.println("GLDrawableHelper " + this + ".invokeGL(): Running runnable");
        }
        runnable.run();
        if (autoSwapBufferMode) {
          drawable.swapBuffers();
        }
      }
    } finally {
      try {
        if (res != GLContext.CONTEXT_NOT_CURRENT) {
          context.release();
        }
      } catch (Exception e) {
      }
      if (lastContext != null) {
        int res2 = lastContext.makeCurrent();
        if (res2 == GLContext.CONTEXT_CURRENT_NEW) {
          lastInitAction.run();
        }
      }
    }
  }
}
