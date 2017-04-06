# Copyright 2017 TWO SIGMA OPEN SOURCE, LLC
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#        http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
from IPython.core.magic import (Magics, magics_class,cell_magic)
from jupyter_client import manager
from ipykernel.kernelbase import Kernel

class BeakerKernelWrapper(Kernel):
    def __init__(self, KernelManager, KernelClient, code,**kwargs):
        super(BeakerKernelWrapper, self).__init__(**kwargs)
        self._execution_count = 1
        self.KernelManager = KernelManager
        self.KernelClient = KernelClient
        self.code = code
        self.run_cell()

    def run_cell(self):
        if not self.KernelManager.is_alive():
            self.send_response(self.iopub_socket, 'stream',
                                 {'name': 'stdout', 'text': 'Restarting kernel "{}"\n'.format(self.KernelManager)})
            self.KernelManager.restart_kernel(now=False)
            self.KernelClient = self.KernelManager.client()
        while self.KernelClient.shell_channel.msg_ready():
            self.KernelClient.shell_channel.get_msg()
        self.KernelClient.execute(self.code, silent=False)
        _execution_state = "busy"
        while _execution_state != 'idle':
            while self.KernelClient.iopub_channel.msg_ready():
                sub_msg = self.KernelClient.iopub_channel.get_msg()
                msg_type = sub_msg['header']['msg_type']
                if msg_type == 'status':
                    _execution_state = sub_msg["content"]["execution_state"]
                else:
                    if msg_type in ('execute_input', 'execute_result'):
                        sub_msg['content']['execution_count'] = self._execution_count
                    self.send_response(self.iopub_socket, msg_type, sub_msg['content'])
        reply = self.KernelClient.get_shell_msg(timeout=10)
        reply['content']['execution_count'] = self._execution_count
        return reply['content']



@magics_class
class BeakerKernelMagics(Magics):
    @cell_magic
    def groovy(self, line, cell):
        self.KernelManager, self.KernelClient = manager.start_new_kernel(startup_timeout=60, kernel_name='groovy')
        return BeakerKernelWrapper(self.KernelManager,self.KernelClient,cell)
ip = get_ipython()
ip.register_magics(BeakerKernelMagics)


if __name__ == '__main__':
    from ipykernel.kernelapp import IPKernelApp
    IPKernelApp.launch_instance(kernel_class=BeakerKernelWrapper)