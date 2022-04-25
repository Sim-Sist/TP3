from functools import partial
from typing import List, Tuple, Union
from matplotlib import pyplot as plt
from numpy import histogram, where
import numpy as np
import pandas as pn


class Probability:
    def __init__(self, dataset: List[float], bins: Union[int, List[float]]) -> None:
        self.dataset = dataset
        self.histogram, self.bin_edges = histogram(dataset, bins=bins)
        self.count = sum(dataset)


def bin_estimates(x) -> Tuple[int, float]:
    q25, q75 = np.percentile(x, [25, 75])
    bin_width: float = 2 * (q75 - q25) * len(x) ** (-1 / 3)
    # bin_width *= 6
    bins: int = round((max(x) - min(x)) / bin_width)
    return bins, bin_width


def get_pdf_values(x):
    bins, bin_width = bin_estimates(x)
    counts, bin_edges = np.histogram(x, bins, density=True)
    y_values = counts
    # total_count = sum(counts)
    # y_values = [c / (bin_width * total_count) for c in counts]
    x_values = [low_edge + bin_width / 2 for low_edge in bin_edges[:-1]]
    return x_values, y_values
