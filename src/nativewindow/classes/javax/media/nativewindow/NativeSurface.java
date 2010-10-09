/**
 * Copyright 2010 JogAmp Community. All rights reserved.
 * Copyright (c) 2010 JogAmp Community. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 * 
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 * 
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY JogAmp Community ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL JogAmp Community OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are those of the
 * authors and should not be interpreted as representing official policies, either expressed
 * or implied, of JogAmp Community.
 */
 
package javax.media.nativewindow;

/** Provides low-level information required for
    hardware-accelerated rendering using a surface in a platform-independent manner.<P>

    A NativeSurface created for a particular on- or offscreen component is
    expected to have the same lifetime as that component. As long as
    the component is alive and realized/visible, NativeSurface must be able
    provide information such as the surface handle while it is locked.<P>
*/
public interface NativeSurface extends SurfaceUpdatedListener {
  /** Unlocked state */
  public static final int LOCK_SURFACE_UNLOCKED = 0;

  /** Returned by {@link #lockSurface()} if the surface is not ready to be locked. */
  public static final int LOCK_SURFACE_NOT_READY = 1;

  /** Returned by {@link #lockSurface()} if the surface is locked, but has changed. */
  public static final int LOCK_SURFACE_CHANGED = 2;

  /** Returned by {@link #lockSurface()} if the surface is locked, and is unchanged. */
  public static final int LOCK_SUCCESS = 3;

  /**
   * Lock the surface of this native window<P>
   *
   * The surface handle, see {@link #lockSurface()}, <br>
   * shall be valid after a successfull call,
   * ie return a value other than {@link #LOCK_SURFACE_NOT_READY}.<P>
   *
   * This call is blocking until the surface has been locked
   * or a timeout is reached. The latter will throw a runtime exception. <P>
   *
   * This call allows recursion from the same thread.<P>
   *
   * The implementation may want to aquire the 
   * application level {@link com.jogamp.common.util.RecursiveToolkitLock}
   * first before proceeding with a native surface lock. <P>
   *
   * @return {@link #LOCK_SUCCESS}, {@link #LOCK_SURFACE_CHANGED} or {@link #LOCK_SURFACE_NOT_READY}.
   *
   * @throws RuntimeException after timeout when waiting for the surface lock
   *
   * @see com.jogamp.common.util.RecursiveToolkitLock
   */
  public int lockSurface();

  /**
   * Unlock the surface of this native window
   *
   * Shall not modify the surface handle, see {@link #lockSurface()} <P>
   *
   * @throws RuntimeException if surface is not locked
   *
   * @see #lockSurface
   * @see com.jogamp.common.util.RecursiveToolkitLock
   */
  public void unlockSurface() throws NativeWindowException ;

  /**
   * Return if surface is locked by another thread, ie not the current one
   */
  public boolean isSurfaceLockedByOtherThread();

  /**
   * Return if surface is locked
   */
  public boolean isSurfaceLocked();

  /**
   * Return the locking owner's Thread, or null if not locked.
   */
  public Thread getSurfaceLockOwner();

  /**
   * Return the lock-exception, or null if not locked.
   *
   * The lock-exception is created at {@link #lockSurface()}
   * and hence holds the locker's call stack.
   */
  public Exception getSurfaceLockStack();

  /**
   * Provide a mechanism to utilize custom (pre-) swap surface
   * code. This method is called before the render toolkit (e.g. JOGL) 
   * swaps the buffer/surface. The implementation may itself apply the swapping,
   * in which case true shall be returned.
   *
   * @return true if this method completed swapping the surface,
   *         otherwise false, in which case eg the GLDrawable 
   *         implementation has to swap the code.
   */
  public boolean surfaceSwap();

  /**
   * Returns the handle to the surface for this NativeSurface. <P>
   * 
   * The surface handle should be set/update by {@link #lockSurface()},
   * where {@link #unlockSurface()} is not allowed to modify it.
   * After {@link #unlockSurface()} it is no more guaranteed 
   * that the surface handle is still valid.
   *
   * The surface handle shall reflect the platform one
   * for all drawable surface operations, e.g. opengl, swap-buffer. <P>
   *
   * On X11 this returns an entity of type Window,
   * since there is no differentiation of surface and window there. <BR>
   * On Microsoft Windows this returns an entity of type HDC.
   */
  public long getSurfaceHandle();

  /** Returns the current width of this surface. */
  public int getWidth();

  /** Returns the current height of this surface. */
  public int getHeight();

  /**
   * Returns the graphics configuration corresponding to this window.
   * @see javax.media.nativewindow.GraphicsConfigurationFactory#chooseGraphicsConfiguration(Capabilities, CapabilitiesChooser, AbstractGraphicsScreen)
   */
  public AbstractGraphicsConfiguration getGraphicsConfiguration();

  /**
   * Convenience: Get display handle from 
   *   AbstractGraphicsConfiguration . AbstractGraphicsScreen . AbstractGraphicsDevice
   */
  public long getDisplayHandle();

  /**
   * Convenience: Get display handle from 
   *   AbstractGraphicsConfiguration . AbstractGraphicsScreen
   */
  public int  getScreenIndex();
  
}

