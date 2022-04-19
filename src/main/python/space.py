from dataclasses import dataclass
from typing import List, Tuple

from utils import get_src


@dataclass
class Position:
    x: float
    y: float


@dataclass
class Speed:
    x: float
    y: float


class Particle:
    def __init__(self, radio: float, mass: float, position: Position, speed: Speed) -> None:
        self.radio = radio
        self.mass = mass
        self.position = position
        self.speed = speed

    def update_position(self, pos: Position):
        if pos is not None:
            self.position = pos

    def update_speed(self, speed: Speed):
        if speed is not None:
            self.speed = speed


class Space:
    def __init__(self, size: float, capacity: int) -> None:
        self.size = size
        self.capacity = capacity
        self.particles: List[Particle] = []

    def add_particle(self, radio: float, mass: float, x: float, y: float, vx: float, vy: float):
        self.particles.append(
            Particle(radio, mass, Position(x, y), Speed(vx, vy)))

    def update_particle(self, index: int, **kwargs):
        position = kwargs.get('pos', None)
        speed = kwargs.get('speed', None)
        self.particles[index].update_position(position)
        self.particles[index].update_speed(speed)


# Read static-info
PATH_TO_OUTPUT_FOLDER = "main/output/"


def get_static_info_name():
    return "static-info000.txt"


static_info_file = open(get_src()+PATH_TO_OUTPUT_FOLDER +
                        get_static_info_name(), "r")
# Read header
size = float(static_info_file.readline())
particles_amount = int(static_info_file.readline())
static_info_file.readline()  # Empty line

space = Space(size, particles_amount)

# Read body
for line in static_info_file:
    data = line.split(" ")
    radio = data[0]
    mass = data[1]
    x = data[2]
    y = data[3]
    vx = data[4]
    vy = data[5]
    color = data[6]
    space.add_particle(radio, mass, x, y, vx, vy)

print(space.particles)
