// Performance mode scene

+ World_Assets {
	performanceMode_ASSET_DEF {
		imagePaths = IdentityDictionary[];
		bufferPaths = IdentityDictionary[
			\performanceMusic -> "../../../assets/3rdparty/PerformanceMode.mp3"
		];
	}
}

+ World_Scene {
	*buildPerformanceModeScene {
		World_Scene.setupScene(26, 17, 50, 50, 1, \performanceMode_ASSET_DEF);
		sceneScripts[\startScene] = {
			{
				1.wait;
				World_Audio.play(\performanceMusic, 1, 2, 1, 1, 0, true, \music)
			}.forkInScene;
		};

		sceneScripts[\keyDown] = {|key|
			switch(key.key)
			// 2
			{50} { World_Scene.createScene(\buildYouDidAlrightScene) }
			// Return
			{16777220} {
				World_Audio.stopEverything;
				World_World.quitGame;
			}
		};

		sceneScripts[\leaveScene] = { World_Audio.release(\performanceMusic) };
	}
}
