from typing import List, Union
from numpy import histogram, where
import pandas as pn


class Probability:
    def __init__(self, dataset: List[float], bins: Union[int, List[float]]) -> None:
        self.dataset = dataset
        self.histogram, self.bin_edges = histogram(dataset, bins=bins)
        self.count = sum(dataset)


data = pn.read_csv(
    "https://archive.ics.uci.edu/ml/machine-learning-databases/iris/iris.data",
    header=None,
)
data.columns = ["sepal_length", "sepal_width", "petal_length", "petal_width", "class"]

iris_setosa = data.loc[where(data["class"] == "Iris-setosa")]  # type: ignore
datalist: List[float] = iris_setosa["petal_length"].tolist()
# print(iris_setosa["petal_width"].tolist())

counts, bin_edges = histogram(datalist, bins=10, density=True)

print(counts)
