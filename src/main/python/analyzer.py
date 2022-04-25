from dataclasses import dataclass
import enum
from functools import partial
import json
import math
from turtle import position
from typing import Any, Iterator, List, Tuple, Union
from matplotlib import use
from matplotlib.font_manager import json_load

from pandas import DataFrame
from space import Path, Space, Velocity, Position
from statistics import mean, stdev
import matplotlib.pyplot as plt
import pylab
from rich.progress import track
import numpy as np

from file_utils import get_src
from prob import bin_estimates, get_pdf_values

# Global vars
PATH_SLASH = "/"
PATH_TO_OUTPUT_FOLDER = "main/output/"
PLOTS_LOCAL_PATH = PATH_TO_OUTPUT_FOLDER + "plots/"
PLOT_EXTENSION = "png"
DATA_FOLDER_LOCLA_PATH = "main/python/data/"
#######################################


def remove_plot_borders():
    plt.gca().spines["top"].set_alpha(0.0)
    plt.gca().spines["bottom"].set_alpha(0.3)
    plt.gca().spines["right"].set_alpha(0.0)
    plt.gca().spines["left"].set_alpha(0.3)


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


def in_data_folder_path(path):
    return get_src() + DATA_FOLDER_LOCLA_PATH + path


def get_static_info_name(filenum: int):
    return f"static-info{filenum:03d}.txt"


def get_dynamic_info_name(filenum: int):
    return f"dynamic-info{filenum:03d}.txt"


def iterr(provider: Iterator[Tuple[int, str]]):
    for pos, speed in provider:
        print(pos)
        print(speed)
        print("-----------------")


def get_path_to_output_file(filename: str):
    return PATH_SLASH.join([get_src(), PATH_TO_OUTPUT_FOLDER, filename]).replace(
        "//", "/"
    )


def process_dynamic_line(line: str) -> Tuple[Position, Velocity]:
    data = line.split(" ")
    return (
        Position(float(data[0]), float(data[1])),
        Velocity(float(data[2]), float(data[3])),
    )


@dataclass
class OutputData:
    amount_of_particles: int = 0
    last_event_num: int = 0
    last_event_time: float = 0
    collision_frec: float = 0
    collision_times = []
    speed_values = []  # This one doesn't include the values for the big particle
    initial_speed_values = []
    mean_kinetic_energy: float = 0
    mean_kinetic_energy_error: float = 0
    time_values = []
    big_particle_path = Path()
    small_particles_displacements = []
    big_particle_displacements = []
    positions = []


def analyze_sim(filenum: int) -> OutputData:
    data = OutputData()
    # Read static-info
    static_info_file = open(get_path_to_output_file(get_static_info_name(filenum)), "r")
    # Read header
    size = float(static_info_file.readline())
    particles_amount = int(static_info_file.readline())
    data.positions = [[] for i in range(particles_amount)]
    static_info_file.readline()  # Empty line

    space = Space(size, particles_amount)

    data.amount_of_particles = space.capacity
    # Read body
    for index, line in enumerate(static_info_file):
        line_data = line.split(" ")
        radio = float(line_data[0])
        # color = line_data[1]
        mass = float(line_data[2])
        space.add_particle(
            index, radio, mass, 0, 0, 0, 0
        )  # add-particle doesn't count for the statistics

    # print(space)
    # print("\n\n")

    static_info_file.close()
    #######
    # Read dynamic info
    dynamic_info_file = open(
        get_path_to_output_file(get_dynamic_info_name(filenum)), "r"
    )

    old_event_time = 0
    # Read body
    while True:
        line = dynamic_info_file.readline()
        if not line:
            break
        last_event_num = int(line)
        data.last_event_time = float(dynamic_info_file.readline())
        dynamic_info_file.readline()  # not interested in colliding particles
        # Read particle's new state
        for i in range(len(space.particles)):
            pos, speed = process_dynamic_line(dynamic_info_file.readline())
            space.update_particle(i, pos, speed)
            data.positions[i].append(pos)

        if last_event_num == 0:
            data.initial_speed_values.extend(space.speed_values())

        data.time_values.append(data.last_event_time)
        data.collision_times.append(data.last_event_time - old_event_time)
        data.speed_values.extend(space.speed_values()[1:])

        data.small_particles_displacements.append(
            [p.displacement() for p in space.particles[1:]]
        )
        data.big_particle_displacements.append(space.particles[0].displacement())

        old_event_time = data.last_event_time

    data.mean_kinetic_energy = space.mean_kinetic_energy()["value"]
    data.mean_kinetic_energy_error = space.mean_kinetic_energy()["error"]
    data.collision_frec = data.last_event_num / data.last_event_time
    data.big_particle_path = space.big_particle_path

    return data


def analyze_sim2(filenum: int):
    time_values = []
    # Read static-info
    static_info_file = open(get_path_to_output_file(get_static_info_name(filenum)), "r")
    # Read header
    size = float(static_info_file.readline())
    particles_amount = int(static_info_file.readline())
    positions = [[] for i in range(particles_amount)]
    static_info_file.readline()  # Empty line

    space = Space(size, particles_amount)

    amount_of_particles = space.capacity
    # Read body
    for index, line in enumerate(static_info_file):
        line_data = line.split(" ")
        radio = float(line_data[0])
        # color = line_data[1]
        mass = float(line_data[2])
        space.add_particle(
            index, radio, mass, 0, 0, 0, 0
        )  # add-particle doesn't count for the statistics

    # print(space)
    # print("\n\n")

    static_info_file.close()
    #######
    # Read dynamic info
    dynamic_info_file = open(
        get_path_to_output_file(get_dynamic_info_name(filenum)), "r"
    )

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
            positions[i].append(pos)

        time_values.append(last_event_time)
        # data.collision_times.append(data.last_event_time - old_event_time)
        # data.speed_values.extend(space.speed_values()[1:])

        old_event_time = last_event_time

    return positions, time_values


def in_plot_output_path(path):
    return get_src() + PLOTS_LOCAL_PATH + path


def plot_values(x_values, y_values, x_name, y_name, title, errors=[0]):

    plt.figure(figsize=(16, 10), dpi=80)
    plt.errorbar(
        x_values,
        y_values,
        yerr=errors,
        ecolor="red",
        marker="o",
    )
    plt.ylim(0, max(y_values) * 1.1)
    plt.xlim(min(x_values), max(x_values))
    # plt.grid(axis="both", alpha=0.3)
    # xtick_density = 10
    # xtick_values = [
    #     min(x_values) + i * ((max(x_values) - min(x_values)) / xtick_density)
    #     for i in range(xtick_density)
    # ]
    # plt.xticks(
    #     xtick_values,
    #     [f"{i:g}" for i in xtick_values],
    # )
    # plt.yticks([min(y_values), mean(y_values), max(y_values)])
    ax_label_fontsize = 25
    plt.xlabel(x_name, fontsize=ax_label_fontsize)
    plt.ylabel(y_name, fontsize=ax_label_fontsize)
    # remove borders
    remove_plot_borders()
    plt.title(title, fontsize=ax_label_fontsize * 1.5)
    # save
    plt.savefig(
        in_plot_output_path("%s.%s" % (title, PLOT_EXTENSION)),
        bbox_inches="tight",
    )
    plt.close()


def load_data():
    for i in range(4):
        data = analyze_sim(i)
        with open(in_data_folder_path(f"time_values{i:03d}.json"), "w") as fp:
            json.dump(data.time_values, fp)
        x, y = data.big_particle_path.get_coordinates()
        with open(in_data_folder_path(f"x{i:03d}.json"), "w") as fp:
            json.dump(x, fp)
        with open(in_data_folder_path(f"y{i:03d}.json"), "w") as fp:
            json.dump(y, fp)

    return


def get_errros(y, x):
    errors = []
    cs = [0.27, 0.28, 0.29, 0.3, 0.31, 0.32, 0.33, 0.34]
    for c in cs:
        temp = []
        for i in range(len(x)):
            temp.append((y[i] - math.sqrt(2 * c * x[i])) ** 2)
        errors.append(sum(temp))
    return errors, cs


def get_diffusivity(msd_values: List[float], time_values: List[int]):
    diffusivities = []
    for index, msd in enumerate(msd_values):
        time = time_values[index]
        diffusivities.append(msd / (2.0 * time) if time != 0 else 0)
    return diffusivities


def get_msd():
    used_time_values = [x for x in range(200)]
    small_msd = []
    big_msd = []
    big_displacements_squared = [[] for x in range(200)]
    for sim in range(10):
        data = analyze_sim(sim)

        next_time = 0
        for index, time in enumerate(data.time_values):
            if math.floor(time) >= next_time:
                next_time = math.floor(time)

                if sim == 5:
                    squared_displ = [
                        x**2 for x in data.small_particles_displacements[index]
                    ]
                    small_msd.append(
                        {
                            "value": mean(squared_displ),
                            "error": stdev(squared_displ),
                        }
                    )
                if index < len(data.big_particle_displacements):
                    big_displacements_squared[next_time].append(
                        data.big_particle_displacements[index] ** 2
                    )

                next_time += 1

    for time in used_time_values:
        big_msd.append(
            {
                "value": mean(big_displacements_squared[time]),
                "error": stdev(big_displacements_squared[time]),
            }
        )
    return big_msd, small_msd, used_time_values


def plot_path(path: Path, title: str, output_format: str = "png"):
    x, y = path.get_coordinates()
    pylab.title(title)
    pylab.plot(x, y)
    pylab.xlim(0, 6)
    pylab.ylim(0, 6)
    ax_label_fontsize = 13
    plt.xlabel("x", fontsize=ax_label_fontsize)
    plt.ylabel("y", fontsize=ax_label_fontsize)
    # remove borders
    remove_plot_borders()

    plt.title(title, fontsize=ax_label_fontsize * 1.5)
    pylab.savefig(
        in_plot_output_path(f"{title.replace('/','|')}.{output_format}"),
        bbox_inches="tight",
        dpi=600,
    )
    pylab.close()


def main():
    # # displacements = [[] for i in range(200)]
    # # used_time_values = [x for x in range(200)]
    # # count = 0
    # # for sim in range(1):
    # #     positions, time_values = analyze_sim2(5)
    # #     # print("pos: " + str(len(positions[0])))
    # #     next_time = 0
    # #     # print("time: " + str(len(time_values)))
    # #     for index, time in enumerate(time_values):

    # #         if math.floor(time) >= next_time:
    # #             next_time = math.floor(time)

    # #             for particle_position in positions[1:]:
    # #                 val = particle_position[index].distance_to(particle_position[0])
    # #                 displacements[next_time].append(val)
    # #             next_time += 1
    # # mean_displacements = []
    # # errors = []
    # # for displacements_in_time_x in displacements:
    # #     val = (
    # #         [d**2 for d in displacements_in_time_x]
    # #         if len(displacements_in_time_x) > 0
    # #         else [0.0, 0.0]
    # #     )
    # #     mean_displacements.append(mean(val))
    # #     errors.append(stdev(val))

    # # # print(displacements)
    # # plt.errorbar(
    # #     used_time_values, mean_displacements, errors, ecolor="red", elinewidth=0.2
    # # )
    # # plt.plot(used_time_values, [math.sqrt(2 * 0.31 * t) for t in used_time_values])
    # # ax_label_fontsize = 13
    # # plt.xlabel("tiempo", fontsize=ax_label_fontsize)
    # # plt.ylabel("DCM", fontsize=ax_label_fontsize)
    # # remove_plot_borders()
    # # plt.title("Desplazamiento Cuadrático Medio", fontsize=ax_label_fontsize * 1.5)

    # # plt.savefig(
    # #     in_plot_output_path(
    # #         "%s.%s" % ("Desplazamiento Cuadrático Medio", PLOT_EXTENSION)
    # #     ),
    # #     bbox_inches="tight",
    # # )

    # # plt.figure()
    # # dif = get_diffusivity(mean_displacements, used_time_values)
    # # plt.plot(used_time_values, dif)
    # # plt.xlabel("tiempo", fontsize=ax_label_fontsize)
    # # plt.ylabel("D", fontsize=ax_label_fontsize)
    # # remove_plot_borders()
    # # plt.title(
    # #     "Coeficiente de difusión",
    # #     fontsize=ax_label_fontsize * 1.5,
    # # )

    # # # plt.savefig(
    # # #     in_plot_output_path("%s.%s" % ("Coeficiente de difusión", PLOT_EXTENSION)),
    # # #     bbox_inches="tight",
    # # # )

    # # plt.figure()

    # # errors, cs = get_errros(mean_displacements, used_time_values)
    # # plt.plot(cs, errors)

    # # plt.xlabel("c", fontsize=ax_label_fontsize)
    # # plt.ylabel("E", fontsize=ax_label_fontsize)
    # # remove_plot_borders()
    # # plt.title(
    # #     "Error del modelo en función de c",
    # #     fontsize=ax_label_fontsize * 1.5,
    # # )

    # # plt.savefig(
    # #     in_plot_output_path(
    # #         "%s.%s" % ("Error del modelo en función de c", PLOT_EXTENSION)
    # #     ),
    # #     bbox_inches="tight",
    # # )

    # plt.savefig()
    # plt.show()
    # compute_simulation_times()
    # return

    plot_collision_times()
    # sim = 2
    # data = analyze_sim(sim)

    # title = "PDF para módulo de velocidades en el estado inicial"
    # var_name = "módulo de velocidad"

    # plot_pdf(data.initial_speed_values, var_name, title)

    # third_of_sim: int = int(float(data.last_event_num) / 3)
    # speed_vals = data.speed_values[-third_of_sim:]
    # title = "PDF para módulo de velocidades en el último tercio"
    # plot_pdf(speed_vals, var_name, title)

    # kinetic_energies = []
    # errors = []
    # speed_values = [0.5, 1, 1.5, 2]
    # for sim, speed in enumerate(speed_values):
    #     data = analyze_sim(sim)
    #     kinetic_energies.append(data.mean_kinetic_energy)
    #     errors.append(data.mean_kinetic_energy_error)
    #     plot_path(
    #         data.big_particle_path, f"Trayectoria para vel. máxima de {speed} m/s"
    #     )

    # plt.errorbar(speed_values, kinetic_energies, errors)
    # plt.show()
    # return


def plot_pdf(data, var_name, title):
    x, y = get_pdf_values(data)
    y_name = "densidad de probabilidad"

    plt.plot(x, y)

    plt.ylim(0, max(y) * 1.1)
    # plt.xlim(0, 1)

    ax_label_fontsize = 13
    plt.xlabel(var_name, fontsize=ax_label_fontsize)
    plt.ylabel(y_name, fontsize=ax_label_fontsize)
    # remove borders
    remove_plot_borders()

    plt.title(title, fontsize=ax_label_fontsize * 1.5)
    # save
    plt.savefig(
        in_plot_output_path("%s.%s" % (title, PLOT_EXTENSION)),
        bbox_inches="tight",
    )
    plt.close()


def plot_collision_times():
    particle_amounts = [100, 120, 140]
    for sim, p_amounts in enumerate(particle_amounts):
        x_name = "tiempo entre colisiones"
        title = f"PDF para tiempos de colisión con {p_amounts} partículas"

        data: OutputData = analyze_sim(sim)
        print("mean: " + str(mean(data.collision_times)))
        print("err: " + str(stdev(data.collision_times)))

        plot_pdf(data.collision_times, x_name, title)


def compute_simulation_times():
    x_name = "numero de simulación"
    y_name = "duración en tiempo de simulación"
    title = "Duración en tiempo de simulación para 20.000 pasos"

    durations = []
    sim_number = []
    for sim in track(range(50)):
        data = analyze_sim(sim)
        durations.append(data.last_event_time)
        sim_number.append(sim)

    with open(in_data_folder_path("sim_durations.json"), "w") as fp:
        json.dump(durations, fp)

    plt.plot(sim_number, durations, marker="o")

    plt.ylim(0, max(durations) * 1.1)
    plt.xlim(0, max(sim_number))

    plt.yticks(
        [round(min(durations), 1), round(mean(durations), 1), round(max(durations), 1)]
    )
    ax_label_fontsize = 13
    plt.xlabel(x_name, fontsize=ax_label_fontsize)
    plt.ylabel(y_name, fontsize=ax_label_fontsize)
    # remove borders
    remove_plot_borders()

    plt.title(title, fontsize=ax_label_fontsize * 1.5)
    # save
    plt.savefig(
        in_plot_output_path("%s.%s" % (title, PLOT_EXTENSION)),
        bbox_inches="tight",
    )
    plt.close()


def test():
    print("No tests")


if __name__ == "__main__":
    main()
    # test()
