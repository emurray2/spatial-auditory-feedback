// Performance mode scene

+ World_Assets {
	performanceMode_ASSET_DEF {
		imagePaths = IdentityDictionary[];
		bufferPaths = IdentityDictionary[
			\performanceMusic -> "../../../assets/3rdparty/PerformanceMode.mp3",
			\performingTimeVoice -> "../../../assets/voice/PerformingTime.mp3"
		];
	}
}

+ World_Scene {
	*buildPerformanceModeScene {
		World_Scene.setupScene(26, 17, 50, 50, 1, \performanceMode_ASSET_DEF);
		sceneScripts[\startScene] = {
			{
				1.wait;
				World_Audio.play(\performanceMusic, 0.1, 2, 1, 1, 0, true, \music)
			}.forkInScene;
			{
				3.wait;
				World_Audio.play(\performingTimeVoice, 1, 0, 1, 1, 0, false, \dialog);
			}.forkInScene;
		};

		sceneScripts[\keyDown] = {|key|
			switch(key.key)
			// 2
			{50} { World_Scene.createScene(\buildYouDidAlrightScene) }
			// 3
			{51} { World_Scene.createScene(\buildYouImprovedScene) }
			// 4
			{52} { World_Scene.createScene(\buildYouCrushedItScene) }
			// 5
			{53} { World_Scene.createScene(\buildDance1Scene) }
			// Return
			{16777220} {
				World_Audio.stopEverything;
				World_World.quitGame;
			}
		};

		sceneScripts[\leaveScene] = {
			World_Audio.release(\performanceMusic);
			World_Audio.release(\performingTimeVoice);
		};
	}
}
