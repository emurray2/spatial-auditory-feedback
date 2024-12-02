// You Improved scene

+ World_Assets {
	youImproved_ASSET_DEF {
		imagePaths = IdentityDictionary[];
		bufferPaths = IdentityDictionary[
			\youImprovedMusic -> "../../../assets/3rdparty/YouImproved.mp3",
			\seriousImprovementVoice -> "../../../assets/voice/SeriousImprovement.mp3"
		];
	}
}

+ World_Scene {
	*buildYouImprovedScene {
		World_Scene.setupScene(26, 17, 50, 50, 1, \youImproved_ASSET_DEF);
		sceneScripts[\startScene] = {
			{
				1.wait;
				World_Audio.play(\youImprovedMusic, 0.1, 2, 1, 1, 0, true, \music)
			}.forkInScene;
			{
				3.wait;
				World_Audio.play(\seriousImprovementVoice, 1, 0, 1, 1, 0, false, \dialog);
			}.forkInScene;
		};

		sceneScripts[\keyDown] = {|key|
			switch(key.key)
			// Return
			{16777220} {
				World_Audio.stopEverything;
				World_World.quitGame;
			}
		};

		sceneScripts[\leaveScene] = {
			World_Audio.release(\youImprovedMusic);
			World_Audio.release(\seriousImprovementVoice);
		};
	}
}
