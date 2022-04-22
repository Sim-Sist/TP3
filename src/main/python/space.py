from asyncio import constants
from curses import pair_content
from dataclasses import dataclass
import math
from statistics import mean
from typing import Iterator, List, Tuple
from scipy import constants

# TODO: falta la presión

# Global vars
DECIMAL_POSITIONS = 3
#######################################


@dataclass
class Position:
    x: float
    y: float

    def __str__(self) -> str:
        return f"pos:({self.x:.{DECIMAL_POSITIONS}f}, {self.y:.{DECIMAL_POSITIONS}f})"

    def distance_to(self, p) -> float:
        return math.sqrt((self.x - p.x) ** 2 + (self.y - p.y) ** 2)


@dataclass
class Velocity:
    x: float
    y: float

    def __str__(self) -> str:
        return (
            f"velocity:({self.x:.{DECIMAL_POSITIONS}f}, {self.y:.{DECIMAL_POSITIONS}f})"
        )

    def module(self):
        return math.sqrt(self.x**2 + self.y**2)


class Path:
    def __init__(self) -> None:
        self.positions = []
        self.displ = []

    def add_position(self, pos: Position):
        self.positions.append(pos)
        self.displ.append(pos.distance_to(self.positions[0]))

    def displacements(self) -> List[float]:
        return self.displ


class Particle:
    def __init__(
        self,
        index: int,
        radio: float,
        mass: float,
        position: Position,
        velocity: Velocity,
    ) -> None:
        self.index = index
        self.radio = radio
        self.mass = mass
        self.position = position
        self.velocity = velocity

    def update_position(self, pos: Position):
        if pos is not None:
            self.position = pos

    def update_velocity(self, velocity: Velocity):
        if velocity is not None:
            self.velocity = velocity

    def __str__(self) -> str:
        return f"{self.index}: {self.position}, {self.velocity}"


class Space:
    def __init__(self, size: float, capacity: int) -> None:
        self.size = size
        self.capacity = capacity
        self.particles: List[Particle] = []
        self.big_particle_path = Path()

    def add_particle(
        self,
        index: int,
        radio: float,
        mass: float,
        x: float,
        y: float,
        vx: float,
        vy: float,
    ):
        self.particles.append(
            Particle(index, radio, mass, Position(x, y), Velocity(vx, vy))
        )

    # def update_particles(self, provider: Iterator[Tuple[Position, Velocity]]) -> None:
    #     self.events += 1
    #     index = 0
    #     for pos, velocity in provider:
    #         if index >= len(self.particles):
    #             break
    #         self.update_particle(index, pos, velocity)
    #         index += 1

    def __kinetic_energy(self, p: Particle) -> float:
        return (1 / 2) * p.mass * (p.velocity.module() ** 2)

    def get_big_part_displacements(self) -> List[float]:
        return self.big_particle_path.displacements()

    def mean_kinetic_energy(self) -> float:
        return mean(self.__kinetic_energy(p) for p in self.particles)

    def temperature(self) -> float:
        return (2 / 3) * (1 / constants.k) * self.mean_kinetic_energy()  # type: ignore

    def speed_values(self) -> List[float]:
        return [p.velocity.module() for p in self.particles]

    def update_particle(
        self, index: int, position: Position, velocity: Velocity
    ) -> None:
        self.particles[index].update_position(position)
        self.particles[index].update_velocity(velocity)
        if index == 0:
            self.big_particle_path.add_position(position)

    def __str__(self) -> str:
        return "\n".join([str(p) for p in self.particles])
