import asyncio
import json
import sys
from functools import reduce
import math


async def cleanup(subprocess_services):
    for subprocess in subprocess_services.values():
        await subprocess.wait()


async def main(subprocess_services):
    num_processors, experiment_jar, config_path, results_path = sys.argv[1:]
    num_processors = int(num_processors)
    with open(config_path) as configFile:
        config = json.load(configFile)

    for slot_id in range(0, num_processors):
        subprocess_services[slot_id] = await asyncio.create_subprocess_exec(
            'java', '-Xmx10G', '-jar', experiment_jar,
            f'{results_path}/nsgaii_results_{slot_id}.txt', f'{results_path}/nsgaii_results_{slot_id}.csv',
            stdout=open(f'{results_path}/out_{slot_id}.log', 'a'),
            stderr=sys.stdout,
            stdin=asyncio.subprocess.PIPE
        )

    slot_id = 0
    job_counts = {slot_id: 0 for slot_id in subprocess_services}
    total_jobs_per_service = reduce(lambda prod, x: x * prod,
                                    map(len, [config['numMobiles'],
                                              config['mapoModelMaxIterations'],
                                              config['populationSize'],
                                              config['injectedSolutionsFraction'],
                                              config['strategyTypes']])
                                    ) / len(subprocess_services)

    for injects in config['injectedSolutionsFraction']:
        for iterations in config['mapoModelMaxIterations']:
            for pop_size in config['populationSize']:
                for num_mobiles in config['numMobiles']:
                    for migration_type in config['strategyTypes']:
                        pop_size = int(round(pop_size))
                        iterations = int(round(iterations))
                        params = list(map(str, (num_mobiles, pop_size, iterations, injects, migration_type)))
                        print(f'Submitting task {params} to {slot_id}')
                        params = (f'{"%.2f" % (100 * job_counts[slot_id] / total_jobs_per_service)}%',
                                  f'{results_path}/nsgaii_results_{slot_id}.txt',
                                  f'{results_path}/nsgaii_results_{slot_id}.csv',
                                  *params)
                        subprocess_services[slot_id].stdin.write(f'{" ".join(params)}\n'.encode())
                        job_counts[slot_id] += 1
                        slot_id = (slot_id + 1) % len(subprocess_services)

    print('finished submitting')
    for subprocess in subprocess_services.values():
        subprocess.stdin.close()


if __name__ == '__main__':
    loop = asyncio.ProactorEventLoop()
    asyncio.set_event_loop(loop)
    new_subprocess_services = {}
    try:
        loop.run_until_complete(main(new_subprocess_services))
    finally:
        loop.run_until_complete(cleanup(new_subprocess_services))
        loop.close()
