package com.intellij.vssSupport;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFileAdapter;
import com.intellij.openapi.vfs.VirtualFileMoveEvent;
import com.intellij.openapi.vfs.VirtualFilePropertyEvent;
import com.intellij.vcsUtil.VcsUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: lloix
 * Date: May 14, 2007
 */
public class ContentRevisionFactory
{
  private static VFSKeysListener listener;
  private static HashMap<FilePath, VssContentRevision> cachedRevisions;

  static
  {
    cachedRevisions = new HashMap<FilePath,VssContentRevision>();
    listener = new VFSKeysListener();
    LocalFileSystem.getInstance().addVirtualFileListener( listener );
  }

  public static void detachListeners()
  {
    LocalFileSystem.getInstance().removeVirtualFileListener( listener );
  }

  private ContentRevisionFactory() {}

  public static VssContentRevision getRevision( @NotNull FilePath path, Project project )
  {
    VssContentRevision revision = cachedRevisions.get( path );
    if( revision == null )
    {
      revision = new VssContentRevision( path, project );
      cachedRevisions.put( path, revision );
    }
    return revision;
  }

  public static void clearCacheForFile( String file )
  {
    FilePath path = VcsUtil.getFilePath( file );
    cachedRevisions.remove( path );
  }

  private static class VFSKeysListener extends VirtualFileAdapter
  {
    public VFSKeysListener() {}

    public void beforeFileMovement( VirtualFileMoveEvent e )
    {
      String oldPath = e.getOldParent().getPath() + "/" + e.getFileName();
      analyzeEvent( oldPath );
    }

    public void beforePropertyChange( VirtualFilePropertyEvent e )
    {
      String oldName = e.getFile().getParent().getPath() + "/" + e.getOldValue();
      analyzeEvent( oldName );
    }

    private static void analyzeEvent( String filePath )
    {
      FilePath path = VcsUtil.getFilePath( filePath );
      VssContentRevision revision = cachedRevisions.get( path );
      if( revision != null )
      {
        cachedRevisions.remove( path );
      }
    }
  }
}