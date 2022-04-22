from typing import Any, Iterator, List, Tuple, Union

from space import Space, Velocity, Position
from file_utils import get_src
from statistics import mean, stdev


# Global vars
PATH_SLASH = "/"
PATH_TO_OUTPUT_FOLDER = "main/output/"
#######################################


def print_brief(l: List[Any], peek_val: int = 3) -> str:
    if peek_val > len(l) / 2:
        raise Exception("peek_value too big")
    ans = (
        "["
        + ",".join([str(x) for x in l[:peek_val]])
        + " ... "
        + ",".join([str(x) for x in l[-peek_val:]])
        + "]"
    )
    return ans


def get_static_info_name():
    return "static-info000.txt"


def get_dynamic_info_name():
    return "dynamic-info000.txt"


def iterr(provider: Iterator[Tuple[int, str]]):
    for pos, speed in provider:
        print(pos)
        print(speed)
        print("-----------------")


def get_path_to_output_file(filename: str):
    return PATH_SLASH.join([get_src(), PATH_TO_OUTPUT_FOLDER, filename])


def process_dynamic_line(line: str) -> Tuple[Position, Velocity]:
    data = line.split(" ")
    return (
        Position(float(data[0]), float(data[1])),
        Velocity(float(data[2]), float(data[3])),
    )


def main():
    ### Variables
    last_event_num: int = 0
    last_event_time: float = 0
    collision_frec: float = 0
    collision_times: List[float] = []
    speed_values: List[
        float
    ] = []  # This one doesn't include the values for the big particle
    temperatures: List[float] = []
    initial_speed_values: List[float] = []
    kinetic_energies: List[float] = []
    displacemets: List[float] = []  # Only for big particle
    ################

    # Read static-info
    static_info_file = open(get_path_to_output_file(get_static_info_name()), "r")
    # Read header
    size = float(static_info_file.readline())
    particles_amount = int(static_info_file.readline())
    static_info_file.readline()  # Empty line

    space = Space(size, particles_amount)

    # Read body
    for index, line in enumerate(static_info_file):
        data = line.split(" ")
        radio = float(data[0])
        mass = float(data[1])
        x = float(data[2])
        y = float(data[3])
        vx = float(data[4])
        vy = float(data[5])
        # color = data[6]
        space.add_particle(index, radio, mass, x, y, vx, vy)

    # print(space)
    print("\n\n")

    static_info_file.close()
    #######
    # Read dynamic info
    dynamic_info_file = open(get_path_to_output_file(get_dynamic_info_name()), "r")

    dynamic_info_file.readline()  # skip empty line

    old_event_time = 0
    # Read body
    while True:
        line = dynamic_info_file.readline()
        if not line:
            break
        last_event_num = int(line)
        last_event_time = float(dynamic_info_file.readline())
        dynamic_info_file.readline()  # not interested in colliding particles
        # Read particle's new state
        for i in range(len(space.particles)):
            pos, speed = process_dynamic_line(dynamic_info_file.readline())
            space.update_particle(i, pos, speed)

        if last_event_num == 0:
            initial_speed_values.extend(space.speed_values())

        collision_times.append(last_event_time - old_event_time)
        speed_values.extend(space.speed_values()[1:])
        temperatures.append(space.temperature())
        kinetic_energies.append(space.mean_kinetic_energy())

        old_event_time = last_event_time

        # print(space)
        # print("\n")

    collision_frec = last_event_num / last_event_time
    displacemets.extend(space.get_big_part_displacements())

    ###### Print everything
    print(f"Collision frec: {collision_frec:.4f}")
    print(f"Collision times mean: {mean(collision_times):.4f}")
    print(f"Kinetic energies: {print_brief(kinetic_energies,3)}")
    print(f"Temperatures: {print_brief(temperatures,3)}")
    print(f"DCM: {mean([x**2 for x in displacemets])}")


def test():
    print("No tests")


if __name__ == "__main__":
    main()
    # test()
