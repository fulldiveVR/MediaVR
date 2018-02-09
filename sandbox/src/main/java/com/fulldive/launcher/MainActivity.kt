package com.fulldive.launcher

import android.os.Bundle
import android.util.Log
import com.fulldive.launcher.scenes.SandboxScene
import com.fulldive.basevr.framework.SceneActivity

open class MainActivity : SceneActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: " + this)
        isMagnetEnabled = false

        sceneManager.skyboxTargetAlpha = 1f
        resourcesManager.addSkyboxRes(R.drawable.skybox_1, "Default")

        resourcesManager.setRecenterButton(true)
        sceneManager.setFpsToLog(true)
        sceneManager.show(SandboxScene(fulldiveContext))
        soundManager.initSounds(this)
    }

    override fun onDestroy() {
        sceneManager.removeAllScenes()
        super.onDestroy()
    }

    companion object {
        val TAG = MainActivity::class.java.simpleName
    }
}