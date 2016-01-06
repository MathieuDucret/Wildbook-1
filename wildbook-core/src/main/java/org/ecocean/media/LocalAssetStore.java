/*
 * This file is a part of Wildbook.
 * Copyright (C) 2015 WildMe
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Foobar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Wildbook.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.ecocean.media;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.ecocean.util.FileUtilities;
import org.ecocean.util.LogBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.samsix.database.DatabaseException;

/**
 * LocalAssetStore references MediaAssets on the current host's
 * filesystem.
 *
 * To create a new store outside of Java, you can directly insert a
 * row into the assetstore table like so (adjusting paths as needed):
 *
 * insert into assetstore (name,type,config,writable) values ('local store', 'LOCAL', '{"root":"/filesystem/path/to/localassetstore","webroot":"http://host/web/path/to/localassetstore"}', true);
 *
 * Then ensure your webserver is configured to serve the filesystem
 * path at the webroot.
 *
 * If you have only one asset store defined, it will be considered the
 * default (see AssetStore.loadDefault()).
 */
public class LocalAssetStore extends AssetStore {
    private static Logger logger = LoggerFactory.getLogger(LocalAssetStore.class);
    private static final String KEY_ROOT = "root";
    private static final String KEY_WEB_ROOT = "webroot";

    private Path root;
    private String webRoot;


    /**
     * Create a new local filesystem asset store.
     *
     * @param name Friendly name for the store.
     *
     * @param root Filesystem path to the base of the asset directory.
     * Must not be null.
     *
     * @param webRoot Base web url under which asset paths are
     * appended.  If null, this store offers no web access to assets.
     *
     * @param wriable True if we are allowed to save files under the
     * root.
     */
    public LocalAssetStore(final String name, final Path root,
                           final String webRoot, final boolean writable)
    {
        this(null, name, makeConfig(root, webRoot), writable);
    }

    /**
     * Create a new local filesystem asset store.  Should only be used
     * internal to AssetStore.buildAssetStore().
     *
     * @param root Filesystem path to the base of the asset directory.
     * Must not be null.
     *
     * @param webRoot Base web url under which asset paths are
     * appended.  If null, this store offers no web access to assets.
     */
    LocalAssetStore(final Integer id, final String name,
                    final AssetStoreConfig config, final boolean writable)
    {
        super(id, name, AssetStoreType.LOCAL, config, writable);
    }

    /**
     * Create our config map.
     */
    private static AssetStoreConfig makeConfig(final Path root, final String webRoot) {
        AssetStoreConfig config = new AssetStoreConfig();

        if (root != null) config.put(KEY_ROOT, root);
        if (webRoot != null) config.put(KEY_WEB_ROOT, webRoot);

        return config;
    }

    public Path root() {
        if (root == null) {
            root = config.getPath(KEY_ROOT);
            if (logger.isInfoEnabled()) {
                logger.info("Asset Store [" + name + "] using root [" + root + "]");
            }
        }
        return root;
    }

    private String webRoot() {
        if (webRoot == null) {
            webRoot = config.getString(KEY_WEB_ROOT);
            if (logger.isInfoEnabled()) {
                logger.info("Asset Store [" + name + "] using web root [" + webRoot + "]");
            }
        }
        return webRoot;
    }

    /**
     * Create a new MediaAsset that points to an existing file under
     * our root.
     *
     * @param path Relative or absolute path to a file.  Must be under
     * the asset store root.
     *
     * @return The MediaAsset, or null if the path is invalid (not
     * under the asset root or nonexistent).
     */
    @Override
    public MediaAsset create(final Path path, final String type) {
        try {
            return new MediaAsset(this, ensurePath(root(), path), type);
        } catch (IllegalArgumentException e) {
            logger.warn("Bad path", e);
            return null;
        }
    }

    /**
     * Create a new MediaAsset that points to an existing file under
     * our root.
     *
     * @param path Relative or absolute path to a file.  Must be under
     * the asset store root.
     *
     * @return The MediaAsset, or null if the path is invalid (not
     * under the asset root or nonexistent).
     */
    @Override
    public MediaAsset create(final String path, final String type) {
        return new MediaAsset(this, ensurePath(root(), Paths.get(path)), type);
    }

    /**
     * Create a new asset from the given form submission part.  The
     * file is copied in to the store as part of this process.
     *
     * @param path Path to copy in.
     *
     * @param path The (optional) subdirectory and (required) filename
     * relative to the asset store root in which to store the file.
     *
     * @param type Probably AssetType.ORIGINAL.
     * @throws DatabaseException
     */
    @Override
    public MediaAsset copyIn(final Path file,
                             final String path,
                             final String category)
        throws IOException
    {
        Path root = root();
        Path subpath = checkPath(root, file);

        Path fullpath = root.resolve(subpath);

        Files.createDirectories(fullpath.getParent());

        if (logger.isDebugEnabled()) {
            logger.debug("copying from " + file + " to " + fullpath);
        }

        Files.copy(file, fullpath, REPLACE_EXISTING);

        return new MediaAsset(this, subpath, category);
    }


    @Override
    public void deleteFrom(final Path path) throws IOException
    {
        if (path == null) {
            return;
        }

        //
        //  TODO: This needs to be genericised because we
        //  currently have midsize and thumbnail on the mediaasset instead
        //  of as child mediaassets as originally planned. That proved to be problematic query-wise.
        //  So, bottom line, this does not handle the other sizes at present.
        //
        FileUtilities.cascadeDelete(getFullPath(path));
    }

    @Override
    public Path getFullPath(final Path path) {
        return Paths.get(root().toString(), path.toString());
    }


    /**
     * Make sure path is under the root, either passed in as a
     * relative path or as an absolute path under the root.
     *
     * @return Subpath to the file relative to the root.
     */
    public static Path checkPath(final Path root, final Path path) {
        if (path == null) throw new IllegalArgumentException("null path");

        Path result = root.resolve(path);
        result = root.relativize(result.normalize());

        if (result.startsWith("..")) {
            throw new IllegalArgumentException("Path not under given root");
        }

        return result;
    }

    /**
     * Like checkPath(), but throws an IllegalArgumentException if the
     * resulting file doesn't exist.
     *
     * @return Subpath to the file relative to the root.
     */
    public static Path ensurePath(final Path root, final Path path) {
        Path result = checkPath(root, path);

        Path full = root.resolve(path);
        if (!full.toFile().exists())
            throw new IllegalArgumentException(full + " does not exist");

        return result;
    }

    /**
     * Return a full URL to the given MediaAsset, or null if the asset
     * is not web-accessible.
     */
    @Override
    public URL webPath(final Path path) {
        if (webRoot() == null) return null;
        if (path == null) return null;

        try {
            if (logger.isDebugEnabled()) {
                logger.debug(LogBuilder.get().appendVar("webroot", webRoot().toString())
                                .appendVar("path", path.toString()).toString());
            }

            URL url;
            if (! path.startsWith("/")) {
                url = new URL(webRoot() + "/" + path.toString());
            } else {
                url = new URL(webRoot() + path.toString());
            }

            if (logger.isDebugEnabled()) {
                logger.debug(LogBuilder.quickLog("url", url.toString()));
            }

            return url;
        } catch (MalformedURLException e) {
            logger.warn("Can't construct web path", e);
            return null;
        }
    }
}
