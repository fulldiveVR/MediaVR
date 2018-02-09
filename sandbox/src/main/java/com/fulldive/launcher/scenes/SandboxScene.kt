package com.fulldive.launcher.scenes

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.support.v4.content.ContextCompat
import com.fulldive.basevr.components.SharedTexture
import com.fulldive.basevr.components.Sprite
import com.fulldive.basevr.components.SpriteBucket
import com.fulldive.basevr.controls.Control
import com.fulldive.basevr.controls.OnControlFocus
import com.fulldive.basevr.controls.SpriteWithLabelControl
import com.fulldive.basevr.controls.menus.AnimationPageMenuAdapter
import com.fulldive.basevr.framework.ActionsScene
import com.fulldive.basevr.framework.FulldiveContext
import com.fulldive.launcher.R
import com.fulldive.mediavr.scenes.camera.CameraScene
import com.fulldive.mediavr.scenes.gallery.GalleryScene
import com.fulldive.mediavr.scenes.vree.VreeScene
import com.fulldive.video.fragments.PageMenuFragment
import java.util.ArrayList

class SandboxScene(fulldiveContext: FulldiveContext) : ActionsScene(fulldiveContext) {
    private val sprites = SpriteBucket()
    private val menuButtons = ArrayList<MenuItem>()
    private lateinit var menuFragment: PageMenuFragment<SpriteWithLabelControl>

    private val pageWidth = 18f
    private val pageHeight = 22f

    override fun onCreate() {
        super.onCreate()

        sprites.addSingleSprite(SharedTexture().also {
            it.setBitmap(BitmapFactory.decodeResource(resources, R.drawable.menu_camera))
        }, "menu_item_camera")

        sprites.addSingleSprite(SharedTexture().also {
            it.setBitmap(BitmapFactory.decodeResource(resources, R.drawable.menu_gallery))
        }, "menu_item_gallery")

        sprites.addSingleSprite(SharedTexture().also {
            it.setBitmap(BitmapFactory.decodeResource(resources, R.drawable.menu_360_images))
        }, "menu_item_pano")

        menuButtons.add(MenuItem("Camera", ITEM_CAMERA, sprites.getSprite("menu_item_camera"), 0))
        menuButtons.add(MenuItem("Gallery", ITEM_GALLERY, sprites.getSprite("menu_item_gallery"), 1))
        menuButtons.add(MenuItem("Gallery 360", ITEM_IMAGES_360, sprites.getSprite("menu_item_pano"), 2))

        menuFragment = PageMenuFragment(fulldiveContext)
        menuFragment.setSize(pageWidth, pageHeight)
        menuFragment.adapter = createAdapter()
        menuFragment.setMenuPadding(0.2f, 1f, 0.2f, 1f)
        addControl(menuFragment)
    }

    override fun onStart() {
        super.onStart()
        sceneManager.skybox = resourcesManager.currentSkybox
    }

    private fun onItemClicked(position: Int) {
        val itemMenu = menuButtons[position]
        val context = resourcesManager.context

        when (itemMenu.id) {
            ITEM_IMAGES_360 -> {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    sceneManager.show(VreeScene(fulldiveContext))
                } else {
                    sceneManager.showPermissionDialog()
                }
            }
            ITEM_GALLERY -> {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    sceneManager.show(GalleryScene(fulldiveContext))
                } else {
                    sceneManager.showPermissionDialog()
                }
            }
            ITEM_CAMERA -> {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    sceneManager.show(CameraScene(fulldiveContext))
                } else {
                    sceneManager.showPermissionDialog()
                }
            }
        }
    }

    private fun createAdapter(): AnimationPageMenuAdapter<SpriteWithLabelControl> {
        return object : AnimationPageMenuAdapter<SpriteWithLabelControl>(parentProvider) {
            override fun getCount(): Int {
                return menuButtons.size
            }

            override fun createControl(width: Float, height: Float): SpriteWithLabelControl {
                val control = SpriteWithLabelControl()
                control.setSize(width, height)
                control.setImageSize(3.5f, 3.5f)
                control.setTextSize(4.2f, .5f)
                control.setOnFocusListener(object : OnControlFocus {
                    override fun onControlFocused(control: Control) {
                        control.scale = 1.05f
                    }

                    override fun onControlUnfocused(control: Control) {
                        control.scale = 1f
                    }
                })
                control.setPivot(0.5f, 0.5f)
                return control
            }

            override fun removeControl(control: SpriteWithLabelControl) {
            }

            override fun bindControl(control: SpriteWithLabelControl, position: Int, index: Int) {
                val item = menuButtons[position]
                control.setSprite(item.sprite)
                control.text = item.title
                control.isVisible = true
                control.setOnClickListener { onItemClicked(position) }
            }

            override fun unbindControl(holder: SpriteWithLabelControl) {
                holder.setSprite(null)
                holder.text = ""
                holder.isVisible = false
            }

            override fun getColumns() = 3

            override fun getRows() = 1
        }
    }

    private class MenuItem(val title: String, val id: Int, val sprite: Sprite?, val sortIndex: Int) : Comparable<MenuItem> {
        override fun compareTo(other: MenuItem) = sortIndex - other.sortIndex
    }

    companion object {
        private val ITEM_IMAGES_360 = 4
        private val ITEM_GALLERY = 6
        private val ITEM_CAMERA = 7
    }
}
