// Practice mode scene

+ World_Assets {
	practiceMode_ASSET_DEF {
		imagePaths = IdentityDictionary[];
		bufferPaths = IdentityDictionary[
			\practiceMusic -> "../../../assets/3rdparty/PracticeMode.mp3",
			\practiceTimeVoice -> "../../../assets/voice/PracticeTime.mp3"
		];
	}
}

+ World_Scene {
	*buildPracticeModeScene {
		World_Scene.setupScene(26, 17, 50, 50, 1, \practiceMode_ASSET_DEF);
		sceneScripts[\startScene] = {
			{
				1.wait;
				World_Audio.play(\practiceMusic, 0.1, 2, 1, 1, 0, true, \music)
			}.forkInScene;
			{
				3.wait;
				World_Audio.play(\practiceTimeVoice, 1, 0, 1, 1, 0, false, \dialog);
			}.forkInScene;
		};

		sceneScripts[\keyDown] = {|key|
			switch(key.key)
			// 1
			{49} { World_Scene.createScene(\buildPerformanceModeScene) }
			// 2
			{50} { World_Scene.createScene(\buildYouDidAlrightScene) }
			// 3
			{51} { World_Scene.createScene(\buildYouImprovedScene) }
			// 4
			{52} { World_Scene.createScene(\buildYouCrushedItScene) }
			// Return
			{16777220} {
				World_Audio.stopEverything;
				World_World.quitGame;
			}
		};

		sceneScripts[\leaveScene] = {
			World_Audio.release(\practiceMusic);
			World_Audio.release(\practiceTimeVoice);
		};
	}
}
