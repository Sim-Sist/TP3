let static, dynamic;

let cantParticles, canvasSize;

let radios = [],
	particles = [];
	events = [];
	color = [];
	collisionParticles = [];

RESIZE_FACTOR = 30;

/*
 * Movimiento Browniano de una particula
*/

function preload() {
	let simNumber = 1;
	static = loadStrings(
		`../output/static-info${simNumber.toString().padStart(3, '0')}.txt`
	);
	dynamic = loadStrings(
		`../output/dynamic-info${simNumber.toString().padStart(3, '0')}.txt`
	);
}

function loadStaticData() {
	canvasSize = static[0] * RESIZE_FACTOR;
	cantParticles = static[1];

	for (let i = 0; i < cantParticles; i++) {
		info = static[i + 2].split(' ');
		radios[i] = float(info[0]);
		color[i] = float(info[1]);
	}
}

let dynamicIndex = 0;

function loadDynamicData() {
	// El archivo de datos dinamicos empieza con un espacio!!
	for (let i = 0; i < cantParticles; i++) {
		if(i = 0){
			events[i] = dynamic[i + 2] // evento 0 tiene guardado el tiempo del primer evento
		}

		collisionParticles = dynamic[i + 3].split(' '); // IDs de particulas que colisionaron
		positionAndVelocity = dynamic[i + 4].split(' ');
		//console.log(radios[i])

		particles[i] = new Particle(
			i, // id
			float(positionAndVelocity[0]),
			float(positionAndVelocity[1]),
			float(positionAndVelocity[2]),
			float(positionAndVelocity[3]),
			radios[i],
			color[i],
		);
		//console.log(particles[i])
	}
	dynamicIndex++;
}

function refresh() {
	for (
		let i = cantParticles * dynamicIndex + 1;
		i < cantParticles * (dynamicIndex + 1);
		i++
	) {
		if(i = cantParticles * dynamicIndex + 1){
			events[dynamicIndex] = dynamic[i] // evento n
		}

		collisionParticles = dynamic[i + 2].split(' '); // IDs de particulas que colisionaron
		positionAndVelocity = dynamic[i + 3].split(' ');
		//console.log(particles[(i - (cantParticles * dynamicIndex + 1))])

		particles[i - (cantParticles * dynamicIndex + 1)].move(
			float(positionAndVelocity[0]),
			float(positionAndVelocity[1]),
			float(positionAndVelocity[2]),
			float(positionAndVelocity[3])
		);
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
	if (frameCount == 1000) {
		noLoop();
	}
}

class Particle {
	// ! Mass variable is not changing anything yet.
	constructor(id, x, y, vx, vy, d, color) {
		this.id = id
		this.x = x * RESIZE_FACTOR;
		this.y = y * RESIZE_FACTOR;
		this.vx = vx;
		this.vy = vy;
		this.d = d * RESIZE_FACTOR;
		this.color = color;
	}

	drawWithColor(color) {
		//noStroke()
		//fill(color)
		//circle(this.x, this.y, this.d)
		//triangle()
		let velocity = createVector(this.vx, this.vy);
		let theta = velocity.heading() + radians(90);
		let r;
		if (this.d == 0) r = 0.05;
		else {
			r = this.d / 2;
		}

		colorMode(HSB, 360, 100, 100);
		let angle = ((atan2(this.vy, this.vx) + PI) / (2 * PI)) * 360;
		//console.log(angle)
		fill(angle, 100, 100);
		stroke(angle, 60, 100);

		push();
		translate(this.x, this.y);
		rotate(theta);
		beginShape();
		vertex(0, -r * 2);
		vertex(-r, r * 2);
		vertex(r, r * 2);
		endShape(CLOSE);
		pop();
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
