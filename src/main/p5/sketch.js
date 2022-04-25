let static, dynamic;

let cantParticles, canvasSize;

let diameters = [],
	particles = [];
events = [];
pColor = [];
collisionParticles = [];

RESIZE_FACTOR = 120;

/*
 * Movimiento Browniano de una particula
 */

function preload() {
	let simNumber = 4;
	static = loadStrings(
		`../output/static-info${simNumber.toString().padStart(3, '0')}.txt`
	);
	dynamic = loadStrings(
		`../output/dynamic-info${simNumber.toString().padStart(3, '0')}.txt`
	);
}

function loadStaticData() {
	canvasSize = static[0] * RESIZE_FACTOR;
	cantParticles = int(static[1]);

	for (let i = 0; i < cantParticles; i++) {
		info = static[i + 3].split(' ');
		diameters[i] = 2 * float(info[0]);
		pColor[i] = info[1];
	}
}

let dynamicIndex = 0;

function loadDynamicData() {
	// El archivo de datos dinamicos empieza con un espacio!!
	for (let i = 0; i < cantParticles; i++) {
		if (i == 0) {
			events[i] = dynamic[i + 1]; // evento 0 tiene guardado el tiempo del primer evento
			collisionParticles[dynamicIndex] = dynamic[i + 2].split(' '); // IDs de particulas que colisionaron
		}

		let positionAndVelocity = dynamic[i + 3].split(' ');
		//console.log(radios[i])

		particles[i] = new Particle(
			i, // id
			float(positionAndVelocity[0]),
			float(positionAndVelocity[1]),
			float(positionAndVelocity[2]),
			float(positionAndVelocity[3]),
			diameters[i],
			pColor[i]
		);
	}
	dynamicIndex++;
}

function refresh() {
	let particleIndex = 0;

	let aux = (cantParticles + 3) * dynamicIndex;

	events[dynamicIndex] = dynamic[aux + 1]; // evento n
	collisionParticles[dynamicIndex] = dynamic[aux + 2].split(' '); // IDs de particulas que colisionaron

	for (let i = aux + 3; i < (cantParticles + 3) * (dynamicIndex + 1); i++) {
		let positionAndVelocity = dynamic[i].split(' ');

		//console.log(particles[(i - (cantParticles * dynamicIndex + 1))])
		particles[particleIndex].move(
			float(positionAndVelocity[0]),
			float(positionAndVelocity[1]),
			float(positionAndVelocity[2]),
			float(positionAndVelocity[3])
		);

		particleIndex++;
	}
	dynamicIndex++;
}

var capturer;
let recordAnimation = false;
let recordingTime = 15; // in seconds MAX:9

function setup() {
	loadStaticData();
	loadDynamicData();
	frameRate(60);
	let canvas = createCanvas(canvasSize, canvasSize);
	canvas.id('canvas');
	if (recordAnimation) {
		capturer = new CCapture({
			format: 'webm',
			framerate: 60,
			verbose: true,
			quelity: 100,
		});
	}

	//frameRate(5)
}

function draw() {
	if (recordAnimation && frameCount == 1) {
		capturer.start();
	}

	background(0, 5, 5);

	refresh();

	// NO USAR
	//saveFrames('image' + frameCount , '.png', 1, 5)

	if (recordAnimation) {
		if (frameCount >= 60 * recordingTime) {
			noLoop();
			capturer.stop();
			capturer.save();
			return;
		}
		capturer.capture(document.getElementById('canvas'));
	}
	if (frameCount == 4999) {
		noLoop();
	}
}

class Particle {
	// ! Mass variable is not changing anything yet.
	constructor(id, x, y, vx, vy, d, color) {
		this.id = id;
		this.x = x * RESIZE_FACTOR;
		this.y = y * RESIZE_FACTOR;
		this.vx = vx;
		this.vy = vy;
		this.d = d * RESIZE_FACTOR;
		this.color = color;
	}

	drawWithColor(color) {
		fill(color);
		circle(this.x, this.y, this.d);
	}

	draw() {
		this.drawWithColor(this.color);
	}

	move(x, y, vx, vy) {
		this.x = x * RESIZE_FACTOR;
		this.y = y * RESIZE_FACTOR;
		this.vx = vx;
		this.vy = vy;
		this.draw();
	}
}
