// Dance 1 scene

+ World_Assets {
	dance1_ASSET_DEF {
		imagePaths = IdentityDictionary[];
		bufferPaths = IdentityDictionary[
			\dance1Music -> "../../../assets/dances/mJS0.mp3"
		];
	}
}

+ World_Scene {
	*buildDance1Scene {
		World_Scene.setupScene(26, 17, 50, 50, 1, \dance1_ASSET_DEF);
		sceneScripts[\startScene] = {
			{
				// The ambisonic panner
				~ambi = { |azim, elev|
					var sig;
					sig = PinkNoise.ar;
					sig = BPF.ar(sig, 5000, 2.0);
					sig = HoaEncodeDirection.ar(sig, azim, elev);
					sig = FoaEncode.ar(sig.keep(AtkFoa.defaultOrder.asHoaOrder.size), ~foaEncode);
					sig = FoaDecode.ar(FoaProximity.ar(HPF.ar(sig, ~freq), AtkHoa.refRadius), ~foaDecode);
					Out.ar(0, sig);
				}.play;
			}.forkInScene;
			{
				World_Audio.play(\dance1Music, 1, 0, 0, 1, 0, false, 'music');
			}.forkInScene;
			{
				JSONlib.parseFile("/home/emurray49/aistplusplus_api/keypoints.json").do {
					|item|
					var a, b, c, d, e;
					// Right wrist
					a = item.at(4);
					// Neck (root joint)
					b = item.at(1);
					// Cartesian coordinates
					c = Cartesian.new(a.at(0), a.at(1), a.at(2));
					// Make coordinates match what supercollider expects
					c = c.transposeYZ;
					c = c.rotate(pi/2);
					d = Cartesian.new(b.at(0) * -1 * a.at(0).sign, b.at(1) * -1 * a.at(1).sign, b.at(2) * -1 * a.at(2).sign);
					// Make coordintates match what supercollider expects
					d = d.transposeYZ;
					d = d.rotate(pi/2);
					// Spherical angles with respect to root joint
					e = c.translate(d).asSpherical;
					~ambi.set(\azim, e.theta);
					~ambi.set(\elev, e.phi);
					// VBlank
					(1/60).wait;
				}
			}.forkInScene
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
			World_Audio.release(\dance1Music);
			~ambi.free;
		};
	}
}
